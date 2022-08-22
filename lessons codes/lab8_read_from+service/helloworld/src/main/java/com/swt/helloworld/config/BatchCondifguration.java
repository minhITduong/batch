package com.swt.helloworld.config;

import com.swt.helloworld.model.Product;
import com.swt.helloworld.reader.ProductServiceAdapter;
import com.swt.helloworld.service.ProductService;
import com.swt.helloworld.writer.ConsoleItemWriter;
import com.swt.helloworld.listener.HwJobExecutionListener;
import com.swt.helloworld.listener.HwStepExecutionListener;
import com.swt.helloworld.processor.InMemeItemProcessor;
import com.swt.helloworld.reader.InMemReader;
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
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

@EnableBatchProcessing
@Configuration
public class BatchCondifguration {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private HwJobExecutionListener hwJobExecutionListener;

    @Autowired
    private HwStepExecutionListener hwStepExecutionListener;

    @Autowired
    private InMemeItemProcessor inMemeItemProcessor;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductServiceAdapter productServiceAdapter;

    public Tasklet helloWorldTasklet(){
        return (new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello world  " );
                return RepeatStatus.FINISHED;
            }
        });
    }

    @Bean
    public Step step1(){
        return steps.get("step1")
                .listener(hwStepExecutionListener)
                .tasklet(helloWorldTasklet())
                .build();
    }

    @Bean
    public InMemReader reader(){
       return new InMemReader();
    }

    @StepScope
    @Bean
    public StaxEventItemReader xmlItemReader(
            @Value( "#{jobParameters['fileInput']}" )
                                                     FileSystemResource inputFile
            ){
        // where to read the xml file
        StaxEventItemReader reader = new StaxEventItemReader();
        reader.setResource(inputFile);
        //need to let reader to know which tags describe the domain object
        reader.setFragmentRootElementName("product");

        // tell reader how to parse XML and which domain object to be mapped
        reader.setUnmarshaller(new Jaxb2Marshaller(){
            {
                setClassesToBeBound(Product.class);
            }
        });

        return reader;

    }

    @StepScope
    @Bean
    public FlatFileItemReader flatFileItemReader(
            @Value( "#{jobParameters['fileInput']}" )
            FileSystemResource inputFile ){
        FlatFileItemReader reader = new FlatFileItemReader();
        // step 1 let reader know where is the file
        reader.setResource( inputFile );

        //create the line Mapper
        reader.setLineMapper(
                new DefaultLineMapper<Product>(){
                    {
                        setLineTokenizer( new DelimitedLineTokenizer() {
                            {
                                setNames( new String[]{"prodId","productName","prodDesc","price","unit"});
                                setDelimiter("|");
                            }
                        });

                        setFieldSetMapper( new BeanWrapperFieldSetMapper<Product>(){
                            {
                                setTargetType(Product.class);
                            }
                        });
                    }
                }

        );
        //step 3 tell reader to skip the header
        reader.setLinesToSkip(1);
        return reader;

    }


    @StepScope
    @Bean
    public FlatFileItemReader flatfixFileItemReader(
            @Value( "#{jobParameters['fileInput']}" )
                    FileSystemResource inputFile ){
        FlatFileItemReader reader = new FlatFileItemReader();
        // step 1 let reader know where is the file
        reader.setResource( inputFile );

        //create the line Mapper
        reader.setLineMapper(
                new DefaultLineMapper<Product>(){
                    {
                        setLineTokenizer( new FixedLengthTokenizer() {
                            {
                                setNames( new String[]{"prodId","productName","productDesc","price","unit"});
                                setColumns(
                                        new Range(1,16),
                                        new Range(17,41),
                                        new Range(42,65),
                                        new Range(66, 73),
                                        new Range(74,80)

                                );
                            }
                        });

                        setFieldSetMapper( new BeanWrapperFieldSetMapper<Product>(){
                            {
                                setTargetType(Product.class);
                            }
                        });
                    }
                }

        );
        //step 3 tell reader to skip the header
        reader.setLinesToSkip(1);
        return reader;

    }


    @Bean
    public JdbcCursorItemReader jdbcCursorItemReader(){
        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        reader.setDataSource(this.dataSource);
        reader.setSql("select product_id, prod_name, prod_desc as productDesc, unit, price from products");
        reader.setRowMapper(new BeanPropertyRowMapper(){
            {
                setMappedClass(Product.class);
            }
        });
        return reader;
    }

    @StepScope
    @Bean
    public JsonItemReader jsonItemReader(
            @Value( "#{jobParameters['fileInput']}" )
                                                   FileSystemResource inputFile){
        JsonItemReader reader = new JsonItemReader(inputFile, new JacksonJsonObjectReader(Product.class));
        return reader;

    }

    @Bean
    public ItemReaderAdapter serviceItemReader(){
        ItemReaderAdapter reader = new ItemReaderAdapter();
        reader.setTargetObject(productServiceAdapter);
        reader.setTargetMethod("nextProduct");

        return reader;
    }



    @Bean
    public Step step2(){
        return steps.get("step2").
                <Integer,Integer>chunk(3)
               // .reader(flatFileItemReader( null ))
               // .reader(xmlItemReader(null))
               // .reader(flatfixFileItemReader(null ))
               // .reader(jdbcCursorItemReader())
              //  .reader(jsonItemReader(null))
                .reader(serviceItemReader())
                .writer(new ConsoleItemWriter())
                .build();
    }

    @Bean
    public Job helloWorldJob(){
        return jobs.get("helloWorldJob")
                .incrementer(new RunIdIncrementer())
                .listener(hwJobExecutionListener)
                .start(step1())
                .next(step2())
                .build();
    }
}
