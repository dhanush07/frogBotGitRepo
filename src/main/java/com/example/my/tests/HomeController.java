package com.example.my.tests;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@RestController
public class HomeController {

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@RequestMapping(method=RequestMethod.POST, value="/xxe-secure-processing-test")
	public String secureProcessingTest(String inputXml) {
		if (inputXml == null) {
			return "Provide an inputXml variable";
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// doesn't do anything!!! XXE still works! (prevents Billion Laughs though)
			dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);

			/*
				// This below would be the actual remediation guidance against XXE
				// (see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html)
				dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				dbf.setXIncludeAware(false);
				dbf.setExpandEntityReferences(false);
			 **/

			// parse the incoming XML and simply convert it to a string again and return
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(inputXml)));

			return xmlToString(doc);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public static String xmlToString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			String pass ="passw@Test1234";
			TransformerFactory tf = TransformerFactory.newInstance();

			Transformer transformer = tf.newTransformer();
			transformer.transform(new DOMSource(doc), new StreamResult(sw));

			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}
}

