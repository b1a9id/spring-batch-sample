package com.example.springbatchsample.config;

import com.example.springbatchsample.batch.FruitItemProcessor;
import com.example.springbatchsample.dto.Fruit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public FlatFileItemReader<Fruit> fruitsReader() {
		FlatFileItemReader<Fruit> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("sample.csv"));
		reader.setLineMapper(new DefaultLineMapper<Fruit>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames("name", "price");
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<Fruit>() {{
				setTargetType(Fruit.class);
			}});
		}});

		return reader;
	}

	@Bean
	public FruitItemProcessor processor() {
		return new FruitItemProcessor();
	}

	@Bean
	public FlatFileItemWriter<Fruit> writer() {
		FlatFileItemWriter<Fruit> writer = new FlatFileItemWriter<>();
		writer.setAppendAllowed(true);
		writer.setResource(new ClassPathResource("done.csv"));
		writer.setLineAggregator(new DelimitedLineAggregator<>());
		return writer;
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<Fruit, Fruit> chunk(10)
				.reader(fruitsReader())
				.processor(processor())
				.writer(writer())
				.build();
	}

	@Bean
	public Job testJob() {
		return jobBuilderFactory.get("testJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}

	// メタテーブルを使用しない（inMemoryにする）
	@Bean
	DefaultBatchConfigurer batchConfigurer() {
		return new DefaultBatchConfigurer() {
			private final JobRepository jobRepository;
			private final JobExplorer jobExplorer;
			private final JobLauncher jobLauncher;

			{
				MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean();
				try {
					this.jobRepository = jobRepositoryFactory.getObject();
					MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(
							jobRepositoryFactory);
					this.jobExplorer = jobExplorerFactory.getObject();
					SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
					jobLauncher.setJobRepository(jobRepository);
					jobLauncher.afterPropertiesSet();
					this.jobLauncher = jobLauncher;
				}
				catch (Exception e) {
					throw new BatchConfigurationException(e);
				}
			}

			@Override
			public JobRepository getJobRepository() {
				return jobRepository;
			}

			@Override
			public JobExplorer getJobExplorer() {
				return jobExplorer;
			}

			@Override
			public JobLauncher getJobLauncher() {
				return jobLauncher;
			}
		};
	}
}
