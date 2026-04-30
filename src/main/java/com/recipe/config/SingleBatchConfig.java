package com.recipe.config;

import com.recipe.domain.entity.Recipe;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@RequiredArgsConstructor
@Configuration
public class SingleBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private static final Pattern MIN_PATTERN  = Pattern.compile("(\\d+)\\s*분");
    private static final Pattern HOUR_PATTERN = Pattern.compile("(\\d+)\\s*시간");


    private String normalizeCookTimeToMinutes(String raw) {
        if (raw == null) return null;

        String s = raw.trim().replaceAll("\\s+", "");
        if (s.isEmpty()) return null;

        boolean isGte = s.contains("이상"); // "2시간이상" -> 120분이상
        String core = s.replace("이내", "").replace("이상", "");

        Integer minutes = parseToMinutes(core);
        if (minutes == null) {
            return raw.trim();
        }

        if (isGte) {
            // 2시간이상 -> 120분이상
            return minutes + "분이상";
        }
        // 2시간이내 -> 120분, 30분이내 -> 30분
        return minutes + "분";
    }

    private Integer parseToMinutes(String s) {
        // "2시간" -> 120
        Matcher hm = HOUR_PATTERN.matcher(s);
        if (hm.matches()) {
            int h = Integer.parseInt(hm.group(1));
            return h * 60;
        }

        // "90분" -> 90
        Matcher mm = MIN_PATTERN.matcher(s);
        if (mm.matches()) {
            return Integer.parseInt(mm.group(1));
        }

        // 혹시 "120" 같이 숫자만 들어오는 경우를 분으로 간주(원치 않으면 제거)
        if (s.matches("\\d+")) {
            return Integer.parseInt(s);
        }

        return null;
    }

    @Bean
    public Job recipeDataMigrationJob() {
        return new JobBuilder("recipeDataMigrationJob", jobRepository)
                .start(recipeDataMigrationStep())
                .build();
    }

    @Bean
    public Step recipeDataMigrationStep() {
        return new StepBuilder("recipeDataMigrationStep", jobRepository)
                .<String[], Recipe>chunk(1000, transactionManager)
        // Chunk 지향 처리: String[] (Reader 출력) -> Recipe (Writer 입력)
                .reader(recipeCsvReader())
                .processor(recipeItemProcessor()) // Reader에서 읽은 String[]을 Recipe 엔티티로 변환
                .writer(recipeDbWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<String[]>  recipeCsvReader() {
        FlatFileItemReader<String[]> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/recipe_data_241226.csv"));
        reader.setEncoding("UTF-8");
        reader.setLinesToSkip(1);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{
                "RCP_SNO", "RCP_TTL", "CKG_NM", "RGTR_ID", "RGTR_NM", "INQ_CNT", "RCMM_CNT", "SRAP_CNT",
                "CKG_MTH_ACTO_NM", "CKG_STA_ACTO_NM", "CKG_MTRL_ACTO_NM", "CKG_KND_ACTO_NM", "CKG_IPDC",
                "CKG_MTRL_CN", "CKG_INBUN_NM", "CKG_DODF_NM", "CKG_TIME_NM", "FIRST_REG_DT", "RCP_IMG_URL"
        });

        reader.setLineMapper(new DefaultLineMapper<String[]>(){{
            setLineTokenizer(tokenizer);
            setFieldSetMapper(new BeanWrapperFieldSetMapper<String[]>(){
                @Override
                public String[] mapFieldSet(FieldSet fieldSet) {
                    return fieldSet.getValues(); // 필드셋을 String 배열로 직접 반환
                }
            });
        }});

        return reader;
    }

    // ItemProcessor 정의 (CSV 데이터 가공 및 엔티티 변환)
    // String[] 타입으로 읽은 CSV 데이터를 Recipe 엔티티로 변환
    // FIRST_REG_DT 파싱 로직 추가.
    @Bean
    public ItemProcessor<String[], Recipe> recipeItemProcessor() {
        return item-> {
            Recipe recipe =  new Recipe();
            recipe.setRcpTtl(item[1]);
            recipe.setCkgNm(item[2]);
            recipe.setInqCnt(item[5] != null && !item[5].isEmpty() ? Integer.valueOf(item[5]) : 0);
            recipe.setRcmmCnt(item[6] != null && !item[6].isEmpty() ? Integer.valueOf(item[6]) : 0);
            recipe.setCkgMthActoNm(item[8]);
            recipe.setCkgMtrlActoNm(item[10]);
            recipe.setCkgKndActoNm(item[11]);


            String rawIngredients = item[13];
            String ingredients = rawIngredients.trim()
                    .replaceAll("^\\[.+?]\\s*", "")
                    //CSV 파일에 공백이 로 되어 공백으로 바꿈 Window Local용
//                    .replaceAll("\u0007", " ")
                    //CSV 파일에 공백으 로 되어 공백으로 바꿈
                    .replaceAll("\\s(\\d+)\\s", "_$1")
                    .replaceAll("\\s*\\|\\s", "/")
                    .replaceAll(" ", "");

            recipe.setCkgMtrlCn(ingredients);
            log.info("변환된 재료들 >>> {}", ingredients);

            recipe.setCkgInbunNm(item[14]);
            recipe.setCkgDodfNm(item[15]);
//            recipe.setCkgTimeNm(item[16]);
            recipe.setCkgTimeNm(normalizeCookTimeToMinutes(item[16]));

            if (item[17] != null && !item[17].isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    recipe.setFirstRegDt(LocalDateTime.parse(item[17], formatter));
                } catch (Exception e) {
                    log.warn("FIRST_REG_DT 파싱 오류: {}", item[17], e);
                    recipe.setFirstRegDt(null); // 파싱 실패 시 null 또는 기본값 설정
                }
            }

            recipe.setRcpImgUrl(item[18]); // RCP_IMG_URL

            return recipe;
        };
    }

    @Bean
    public JpaItemWriter<Recipe> recipeDbWriter() {
        JpaItemWriter<Recipe> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}

