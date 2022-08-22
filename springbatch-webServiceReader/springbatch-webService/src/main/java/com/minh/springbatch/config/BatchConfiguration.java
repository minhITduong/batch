package com.minh.springbatch.config;

import com.minh.springbatch.listener.HwJobExecutionListener;
import com.minh.springbatch.listener.HwStepExecutionListener;
import com.minh.springbatch.mapper.ProductFieldSetMapper;
import com.minh.springbatch.mapper.ProductFieldSetMapperByName;
import com.minh.springbatch.model.Product;
import com.minh.springbatch.model.ProductXml;
import com.minh.springbatch.processor.InMemItemProcessor;
import com.minh.springbatch.reader.InMemReader;
import com.minh.springbatch.writer.ConsoleItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private HwJobExecutionListener hwJobExecutionListener;

    @Autowired
    private HwStepExecutionListener hwStepExecutionListener;

    @Autowired
    private InMemItemProcessor inMemItemProcessor;

    public Tasklet helloWorldTasklet () {
        return (new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello world");
                return RepeatStatus.FINISHED;
            }
        });
    }

    @Bean
    public Step step1 () {
        return steps.get("step1")
                .listener(hwStepExecutionListener)
                .tasklet(helloWorldTasklet())
                .build();
    }

    @Bean
    public InMemReader reader () {
        return new InMemReader();
    }

    @StepScope
    @Bean
    public FlatFileItemReader flatFileItemReader (
         ) {

        FlatFileItemReader itemReader = new FlatFileItemReader();

        //step 1 let reader knows where is the file
        itemReader.setResource(new FileSystemResource("input/product.csv"));

        //step 2 create the line Mapper
        DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();
        //DelimitedLineTokenizer defaults to comma as its delimiter
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(new ProductFieldSetMapper());
        itemReader.setLineMapper(lineMapper);
        itemReader.open(new ExecutionContext());

        //step 3
        itemReader.setLinesToSkip(1);

        //Product product = itemReader.read();
        return itemReader;
    }

    @StepScope
    @Bean
    public FlatFileItemReader flatFileItemReaderByName (
            @Value("#{jobParameters['inputFile']}")
            FileSystemResource inputFile
    ) {

        FlatFileItemReader itemReader = new FlatFileItemReader();

        /*Pass file name as parameter and use Stepscope*/
        //step 1 let reader knows where is the file
        itemReader.setResource(inputFile);

        //step 2 create the line Mapper
        DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();
        //DelimitedLineTokenizer defaults to comma as its delimiter

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(new String[] {"productID", "productName", "ProductDesc", "price", "unit"});

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(new ProductFieldSetMapper());
        lineMapper.setFieldSetMapper(new ProductFieldSetMapperByName());
        itemReader.setLineMapper(lineMapper);
        itemReader.open(new ExecutionContext());

        //step 3
        itemReader.setLinesToSkip(1);

        //Product product = itemReader.read();
        return itemReader;
    }

    @Bean
    public StaxEventItemReader xmlItemReader() {
        return new StaxEventItemReaderBuilder<ProductXml>()
                .name("xmlItemReader")
                .resource(new FileSystemResource("input/product.xml") )
                .addFragmentRootElements("product")
                .unmarshaller(productMarshaller())
                .build();
    }

    @Bean
    public XStreamMarshaller productMarshaller() {
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("product", ProductXml.class);
        aliases.put("productId", Integer.class);
        aliases.put("productName", String.class);
        aliases.put("productDesc", String.class);
        aliases.put("unit", Integer.class);
        aliases.put("price", BigDecimal.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();

        marshaller.setAliases(aliases);

        return marshaller;
    }

    @Bean Step step2 () {
        return steps.get("step2")
                .<Integer, Integer>chunk(3)
                //.reader(flatFileItemReaderByName(null))
                .reader(xmlItemReader())
                .writer(new ConsoleItemWriter())
                .build();
    }

    @Bean
    public Job helloWorldJob () {
        return jobs.get("helloWorldJob")
                .incrementer(new RunIdIncrementer())
                .listener(hwJobExecutionListener)
                .start(step1())
                .next(step2())
                .build();
    }


}
