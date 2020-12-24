package com.catascopic.gateway;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class Test {

	public static void main(String[] args) throws Exception {
		Path base = Paths.get("C:\\Users\\mkoren.AIS-DEV\\Documents\\schemas\\arh\\2\\CVE\\Schema\\ogc\\gml\\3.2.1\\gmlBase.xsd");
		Path xlink = base.resolveSibling("../../../w3/1999/xlink/xlink.xsd");
		System.out.println(xlink);
		System.out.println(Files.exists(xlink));
	}



}
