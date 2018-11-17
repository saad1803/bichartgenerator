package com.barchart.bi.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.barchart.bi.dto.PoiDTO;

@Service
public class BarChartGenerator {
	
	@Value("${source.file.path}")
	private String filePath;
	
	@Value("${target.file.name}")
	private String targetFileName;
	
	@Autowired
	private EmailServiceImpl email;
	
	private List<PoiDTO> container = new ArrayList<PoiDTO>();
	
	public void csvFileReader(String filePath) {

		//read file into stream, try-with-resources
		List<String> list = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {

			list = br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
	
		list.forEach((val)-> {
			String[] token = val.split(",");
			System.out.println(token[1] + "-------------" + token[2]);
			PoiDTO temp = new PoiDTO();
			temp.setName(token[1]);
			temp.setVal(token[2]);
			container.add(temp);
		});
			
		
	}
	
    public List<PoiDTO> getContainer() {
		return container;
	}



	public void setContainer(List<PoiDTO> container) {
		this.container = container;
	}
	
	public void barChartGenerate() throws Exception {
		/* Read the bar chart data from the excel file */
        
        csvFileReader(filePath);
		
        /* HSSFWorkbook object reads the full Excel document. We will manipulate this object and
        write it back to the disk with the chart */
        HSSFWorkbook my_workbook = new HSSFWorkbook();
        
        /* Read chart data worksheet */
        HSSFSheet my_sheet = my_workbook.createSheet("Sheet 1");
        /* Create Dataset that will take the chart data */
        DefaultCategoryDataset my_bar_chart_dataset = new DefaultCategoryDataset();
        
        
        for(PoiDTO temp: container) {
        	my_bar_chart_dataset.addValue(new Double(temp.getVal()), temp.getName(), temp.getName());
        }
        /* Create a logical chart object with the chart data collected */
        JFreeChart BarChartObject=ChartFactory.createBarChart("LO Table Counts","Counts","Table Name",my_bar_chart_dataset,PlotOrientation.VERTICAL,true,true,false);  
        /* Dimensions of the bar chart */               
        int width=1000; /* Width of the chart */
        int height=640; /* Height of the chart */               
        /* We don't want to create an intermediate file. So, we create a byte array output stream 
        and byte array input stream
        And we pass the chart data directly to input stream through this */             
        /* Write chart as PNG to Output Stream */
        ByteArrayOutputStream chart_out = new ByteArrayOutputStream();          
        ChartUtilities.writeChartAsPNG(chart_out,BarChartObject,width,height);
        /* We can now read the byte data from output stream and stamp the chart to Excel worksheet */
        int my_picture_id = my_workbook.addPicture(chart_out.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        /* we close the output stream as we don't need this anymore */
        chart_out.close();
        /* Create the drawing container */
        HSSFPatriarch drawing = my_sheet.createDrawingPatriarch();
        /* Create an anchor point */
        ClientAnchor my_anchor = new HSSFClientAnchor();
        /* Define top left corner, and we can resize picture suitable from there */
        my_anchor.setCol1(4);
        my_anchor.setRow1(5);
        /* Invoke createPicture and pass the anchor point and ID */
        HSSFPicture  my_picture = drawing.createPicture(my_anchor, my_picture_id);
        /* Call resize method, which resizes the image */
        my_picture.resize();
                       
        /* Write changes to the workbook */
        FileOutputStream out = new FileOutputStream(new File(targetFileName));
        my_workbook.write(out);
        out.close();
        
	}
	
	public void generateHtml() throws Exception {
		
		csvFileReader(filePath);
		
		StringBuffer sb = new StringBuffer();
		for(PoiDTO temp: container) {
			sb.append("<tr style=\\\"background: #E6EED5\\\">");
			sb.append(System.lineSeparator());
			sb.append("<td style=\\\"border: 1px solid #B3CC82; border-collapse: collapse; padding: 3px;\\\">");
			sb.append(temp.getName());
			sb.append("</td>");
			sb.append(System.lineSeparator());
			sb.append("<td style=\\\"border: 1px solid #B3CC82; border-collapse: collapse; padding: 3px;\\\">");
			sb.append(temp.getVal());
			sb.append("</td>");
			sb.append(System.lineSeparator());
		}
		
		
		ClassPathResource cpr = new ClassPathResource("emailBodyTemplate.txt");
		byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
		String emailBody = new String(bdata);
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		
		emailBody = emailBody.replaceAll("%%DATE%%", dtf.format(now));
		emailBody = emailBody.replaceAll("%%CONTENT%%", sb.toString());
		
		email.sendmail(emailBody);
	}

}
