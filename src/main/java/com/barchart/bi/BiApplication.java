package com.barchart.bi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.barchart.bi.services.BarChartGenerator;

@SpringBootApplication
public class BiApplication implements CommandLineRunner {

	private static Logger logger = LoggerFactory
		      .getLogger(BiApplication.class);

	@Value("${report.type}")
	private String reportType;
	
	@Autowired
	public BarChartGenerator bcGenerator;
	
	public static void main(String[] args) {
		logger.info("STARTING THE APPLICATION");
		SpringApplication.run(BiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("EXECUTING : command line runner");
        
        try {
        	if(reportType.equalsIgnoreCase("BARCHART")) {
        		bcGenerator.barChartGenerate();
        	}else if (reportType.equals("HTML")) {
        		bcGenerator.generateHtml();
        	}
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}
        
        logger.info("Execution Finished");
		
	}
}
