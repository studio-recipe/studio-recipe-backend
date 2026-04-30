package com.recipe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchJobController {

    private final JobLauncher jobLauncher; // Spring Batch Job을 실행하는 핵심 컴포넌트
    private final Job recipeDataMigrationJob; // BatchConfig에서 @Bean으로 정의한 Job Bean

    @GetMapping("/run-recipe-csv")
    public ResponseEntity<String> runRecipeCsv() {
        try{
        //Job 실행 시 Job 인스턴스 구분하기 위한 고유 값
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", String.valueOf(System.currentTimeMillis()))
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

            log.info("Batch Job '{}' 실행 요청. Parameters: {}", recipeDataMigrationJob.getName(), jobParameters);

            //jobLauncher.run(recipeDataMigrationJob,  jobParameters);
            // jobLauncher.run() 호출 실제 Job 실행
            JobExecution jobExecution = jobLauncher.run(recipeDataMigrationJob, jobParameters);

            log.info("Batch Job '{}' 실행 완료. Status: {}", recipeDataMigrationJob.getName(), jobExecution.getStatus());
            return ResponseEntity.ok("Batch Job '" + recipeDataMigrationJob.getName() + "' initiated. Status: " + jobExecution.getStatus());
        } catch (Exception e) {
            log.error("Batch Job 실행 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error initiating batch job: " + e.getMessage());
        }
    }
}
