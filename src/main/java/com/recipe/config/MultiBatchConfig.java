//package com.recipe.config;
//
//import com.recipe.domain.entity.Recipe;
//import jakarta.persistence.EntityManagerFactory;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.database.JpaItemWriter;
//import org.springframework.batch.item.file.FlatFileItemReader;
//import org.springframework.batch.item.file.MultiResourceItemReader;
//import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
//import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
//import org.springframework.batch.item.file.mapping.DefaultLineMapper;
//import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Configuration
//@Log4j2
//@RequiredArgsConstructor
//public class BatchConfig {
//    private final JobRepository jobRepository;
//    private final PlatformTransactionManager transactionManager;
//    private final EntityManagerFactory entityManagerFactory;
//
//    // --- (Job 및 Step 정의는 기존과 동일) ---
//
//    @Bean
//    public Job recipeDataMigrationJob() throws IOException {
//        return new JobBuilder("recipeDataMigrationJob", jobRepository)
//                .start(recipeDataMigrationStep())
//                .build();
//    }
//
//    @Bean
//    public Step recipeDataMigrationStep() throws IOException {
//        return new StepBuilder("recipeDataMigrationStep", jobRepository)
//                .<String[], Recipe>chunk(1000, transactionManager)
//                .reader(multiFileRecipeCsvReader())
//                .processor(recipeItemProcessor())
//                .writer(recipeDbWriter())
//                .build();
//    }
//
//    @Bean
//    public MultiResourceItemReader<String[]> multiFileRecipeCsvReader() throws IOException {
//        MultiResourceItemReader<String[]> reader = new MultiResourceItemReader<>();
//
//        // src/main/resources/data/ 아래에 있는 모든 .csv 파일을 찾습니다.
//        // 또는 특정 경로 아래의 모든 CSV 파일: new PathMatchingResourcePatternResolver().getResources("file:/path/to/your/folder/*.csv")
//        reader.setResources(new PathMatchingResourcePatternResolver().getResources("classpath:data/*.csv")); // CSV 파일이 위치한 경로 및 패턴 설정
//        reader.setDelegate(flatFileRecipeReader()); // 각 파일을 실제로 읽을 FlatFileItemReader를 delegate로 설정
//
//        return reader;
//    }
//
//
//    // --- FlatFileItemReader 정의 (MultiResourceItemReader의 delegate로 사용될 기본 리더) ---
//    // 주의: setResource()는 MultiResourceItemReader에서 설정하므로 여기서는 제거합니다.
//    // 각 파일은 이 템플릿 리더를 통해 읽혀집니다.
//    private ResourceAwareItemReaderItemStream<? extends String[]> flatFileRecipeReader() {
//        FlatFileItemReader<String[]> reader = new FlatFileItemReader<>();
//        reader.setEncoding("UTF-8");
//        reader.setLinesToSkip(1); // 각 파일의 첫 번째 줄(헤더) 건너뛰기
//
//        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
//        tokenizer.setNames(new String[]{
//                "RCP_SNO", "RCP_TTL", "CKG_NM", "RGTR_ID", "RGTR_NM", "INQ_CNT", "RCMM_CNT", "SRAP_CNT",
//                "CKG_MTH_ACTO_NM", "CKG_STA_ACTO_NM", "CKG_MTRL_ACTO_NM", "CKG_KND_ACTO_NM", "CKG_IPDC",
//                "CKG_MTRL_CN", "CKG_INBUN_NM", "CKG_DODF_NM", "CKG_TIME_NM", "FIRST_REG_DT", "RCP_IMG_URL"
//        });
//
//        reader.setLineMapper(new DefaultLineMapper<>() {{
//            setLineTokenizer(tokenizer);
//            setFieldSetMapper(new BeanWrapperFieldSetMapper<String[]>() {
//                @Override
//                public String[] mapFieldSet(org.springframework.batch.item.file.transform.FieldSet fieldSet) {
//                    return fieldSet.getValues();
//                }
//            });
//        }});
//
//        return reader;
//    }
//
//    // --- (ItemProcessor와 ItemWriter 정의는 기존과 동일) ---
//    @Bean
//    public ItemProcessor<String[], Recipe> recipeItemProcessor() {
//        // ... 기존 ItemProcessor 로직 ... (위 답변의 코드 참조)
//        return item -> {
//            Recipe recipe = new Recipe();
//            // ... (Recipe 엔티티에 값 매핑 로직) ...
//
//            // FIRST_REG_DT (LocalDateTime 파싱)
//            if (item[17] != null && !item[17].isEmpty()) {
//                try {
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                    recipe.setFirstRegDt(LocalDateTime.parse(item[17], formatter));
//                } catch (Exception e) {
//                    log.warn("FIRST_REG_DT 파싱 오류: {}", item[17], e);
//                    recipe.setFirstRegDt(null);
//                }
//            }
//            recipe.setRcpTtl(item[1]); // 예시
//            recipe.setCkgNm(item[2]);  // 예시
//            recipe.setInqCnt(item[5] != null && !item[5].isEmpty() ? Integer.valueOf(item[5]) : 0);
//            recipe.setRcmmCnt(item[6] != null && !item[6].isEmpty() ? Integer.valueOf(item[6]) : 0);
//            recipe.setCkgMthActoNm(item[8]);
//            recipe.setCkgMtrlActoNm(item[10]);
//            recipe.setCkgKndActoNm(item[11]);
//            recipe.setCkgMtrlCn(item[13]);
//            recipe.setCkgInbunNm(item[14]);
//            recipe.setCkgDodfNm(item[15]);
//            recipe.setCkgDodfNm(item[16]);
//            recipe.setCkgTimeNm(item[16]);
//            recipe.setRcpImgUrl(item[18]);
//
//            return recipe;
//        };
//    }
//
//
//    @Bean
//    public JpaItemWriter<Recipe> recipeDbWriter() {
//        JpaItemWriter<Recipe> writer = new JpaItemWriter<>();
//        writer.setEntityManagerFactory(entityManagerFactory);
//        return writer;
//    }
//}
