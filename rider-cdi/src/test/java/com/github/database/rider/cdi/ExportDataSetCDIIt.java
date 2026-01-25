package com.github.database.rider.cdi;

import static org.assertj.core.api.Assertions.*;

import java.io.File;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
public class ExportDataSetCDIIt {
	private static final String NEW_LINE = System.getProperty("line.separator");

	@Inject
    EntityManager em;

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML,outputName="target/exported/xml/allTables.xml")
	public void shouldExportAllTablesInXMLFormat() {
	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML_DTD, outputName = "target/exported/xml_dtd/allTables.xml")
	public void shouldExportAllTablesInXMLAndDTDFormat() {
	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.YML,outputName="target/exported/yml/allTables")
	public void shouldExportAllTablesInYMLFormatOmmitingExtension() {
	}

	@Test
	@ExportDataSet(outputName="target/exported/yml/generatedWithoutDataSetAnnotation")
	public void shouldExportAllTablesInYMLFormatWithoutDataSetAnnotation() {
		//seed database
		DataSetExecutorImpl.getExecutorById(DataSetProcessor.CDI_DBUNIT_EXECUTOR)
				.createDataSet(new DataSetConfig("datasets/yml/users.yml"));
	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML, queryList = {"select * from USER u where u.ID = 1"}, outputName="target/exported/xml/filtered.xml")
	public void shouldExportXMLDataSetUsingQueryToFilterRows() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML_DTD,
			queryList = { "select * from USER u where u.ID = 1" },
			outputName = "target/exported/xml_dtd/filtered.xml")
	public void shouldExportXMLAndDTDDataSetUsingQueryToFilterRows() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.YML, queryList = {"select * from USER u where u.ID = 1"}, outputName="target/exported/yml/filtered.yml")
	public void shouldExportYMLDataSetUsingQueryToFilterRows() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML, queryList = {"select * from USER u where u.ID = 1"}, includeTables = {"TWEET"}, outputName="target/exported/xml/filteredIncludes.xml")
	public void shouldExportXMLDataSetUsingQueryAndIncludesToFilterRows() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML_DTD,
			queryList = { "select * from USER u where u.ID = 1" },
			includeTables = { "TWEET" },
			outputName = "target/exported/xml_dtd/filteredIncludes.xml")
	public void shouldExportXMLAndDTDDataSetUsingQueryAndIncludesToFilterRows() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.YML, queryList = {"select * from USER u where u.ID = 1"}, includeTables = "TWEET", outputName="target/exported/yml/filteredIncludes.yml")
	public void shouldExportYMLDataSetUsingQueryAndIncludesToFilterRows() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML, includeTables = "USER", outputName="target/exported/xml/includes.xml")
	public void shouldExportXMLDataSetWithTablesInIncludes() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML_DTD,
			includeTables = "USER",
			outputName = "target/exported/xml_dtd/includes.xml")
	public void shouldExportXMLAndDTDDataSetWithTablesInIncludes() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.YML, includeTables = "USER", outputName="target/exported/yml/includes.yml")
	public void shouldExportYMLDataSetWithTablesInIncludes() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML, includeTables = "USER", dependentTables = true, outputName="target/exported/xml/dependentTables.xml")
	public void shouldExportXMLDataSetUsingIncludesWithDependentTables() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.XML_DTD,
			includeTables = "USER",
			dependentTables = true,
			outputName = "target/exported/xml_dtd/dependentTables.xml")
	public void shouldExportXMLAndDTDDataSetUsingIncludesWithDependentTables() {

	}

	@Test
	@DataSet("datasets/yml/users.yml")
	@ExportDataSet(format = DataSetFormat.YML, includeTables = {"USER","TWEET"}, dependentTables = true, outputName="target/exported/yml/dependentTables.yml")
	public void shouldExportYMLDataSetUsingIncludesWithDependentTables() {

	}


	@AfterClass
	public static void assertGeneratedDataSets() {
		assertXMLFileContent("target/exported/xml/allTables.xml");

		assertXMLFileContent("target/exported/xml_dtd/allTables.xml");
		assertDTDFileContent("target/exported/xml_dtd/allTables.dtd");

		//xmlDataSetWithAllTables.delete();

		File ymlDataSetWithAllTables = new File("target/exported/yml/allTables.yml");
		assertThat(ymlDataSetWithAllTables).exists();
		assertThat(contentOf(ymlDataSetWithAllTables)).
				contains("FOLLOWER:"+NEW_LINE +
						"  - ID: 1"+NEW_LINE +
						"    USER_ID: 1"+NEW_LINE +
						"    FOLLOWER_ID: 2"+NEW_LINE );

		assertThat(contentOf(ymlDataSetWithAllTables)).
				contains("USER:"+NEW_LINE +
						"  - ID: 1"+NEW_LINE +
						"    NAME: \"@realpestano\""+NEW_LINE +
						"  - ID: 2"+NEW_LINE +
						"    NAME: \"@dbunit\"");


		assertXMLFileContentFiltered("target/exported/xml/filtered.xml");

		assertXMLFileContentFiltered("target/exported/xml_dtd/filtered.xml");
		assertDTDFileContentFiltered("target/exported/xml_dtd/filtered.dtd");

		File ymlFilteredDataSet = new File("target/exported/yml/filtered.yml");
		assertThat(ymlFilteredDataSet).exists();
		assertThat(contentOf(ymlFilteredDataSet)).contains("USER:"+NEW_LINE +
				"  - ID: 1"+NEW_LINE +
				"    NAME: \"@realpestano\"");

		assertXMLFileContentFilteredIncludes("target/exported/xml/filteredIncludes.xml");

		assertXMLFileContentFilteredIncludes("target/exported/xml_dtd/filteredIncludes.xml");
		assertDTDFileContentFilteredIncludes("target/exported/xml_dtd/filteredIncludes.dtd");

		File ymlFilteredIncludesDataSet = new File("target/exported/yml/filteredIncludes.yml");
		assertThat(ymlFilteredIncludesDataSet).exists();
		assertThat(contentOf(ymlFilteredIncludesDataSet)).contains("USER:" + NEW_LINE +
				"  - ID: 1" + NEW_LINE +
				"    NAME: \"@realpestano\"");

		assertThat(contentOf(ymlFilteredIncludesDataSet)).
				contains("TWEET:"+NEW_LINE +
						"  - ID: \"abcdef12233\""+NEW_LINE +
						"    CONTENT: \"dbunit rules!\""+NEW_LINE +
						"    USER_ID: 2"+NEW_LINE +
						"  - ID: \"abcdef12345\""+NEW_LINE +
						"    CONTENT: \"dbunit rules!\""+NEW_LINE +
						"    USER_ID: 1"+NEW_LINE +
						"  - ID: \"abcdef1343\""+NEW_LINE +
						"    CONTENT: \"CDI for the win!\""+NEW_LINE +
						"    USER_ID: 2");

		assertXMLFileContentDependent("target/exported/xml/dependentTables.xml");

		assertXMLFileContentDependent("target/exported/xml_dtd/dependentTables.xml");
		assertDTDFileContentDependent("target/exported/xml_dtd/dependentTables.dtd");

		File ymlDependentTablesDataSet = new File("target/exported/yml/dependentTables.yml");
		assertThat(ymlDependentTablesDataSet).exists();
		assertThat(contentOf(ymlDependentTablesDataSet)).contains("USER:"+NEW_LINE +
				"  - ID: 1"+NEW_LINE +
				"    NAME: \"@realpestano\""+NEW_LINE +
				"  - ID: 2"+NEW_LINE +
				"    NAME: \"@dbunit\"");

		assertThat(contentOf(ymlDependentTablesDataSet)).
				contains("TWEET:"+NEW_LINE +
						"  - ID: \"abcdef12233\""+NEW_LINE +
						"    CONTENT: \"dbunit rules!\""+NEW_LINE +
						"    USER_ID: 2"+NEW_LINE +
						"  - ID: \"abcdef12345\""+NEW_LINE +
						"    CONTENT: \"dbunit rules!\""+NEW_LINE +
						"    USER_ID: 1"+NEW_LINE +
						"  - ID: \"abcdef1343\""+NEW_LINE +
						"    CONTENT: \"CDI for the win!\""+NEW_LINE +
						"    USER_ID: 2");

		assertThat(contentOf(ymlDependentTablesDataSet)).
				contains("FOLLOWER:"+NEW_LINE +
						"  - ID: 1"+NEW_LINE +
						"    USER_ID: 1"+NEW_LINE +
						"    FOLLOWER_ID: 2");
	}

	private static void assertXMLFileContent(String filename) {
		File xmlDataSetWithAllTables = new File(filename);
		assertThat(xmlDataSetWithAllTables).exists();

		assertThat(contentOf(xmlDataSetWithAllTables)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
				.contains("<USER ID=\"2\" NAME=\"@dbunit\"/>")
				.contains("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");
	}

	private static void assertDTDFileContent(String filename) {
		File dtdDataSetWithAllTables = new File(filename);
		assertThat(dtdDataSetWithAllTables).exists();

		assertThat(contentOf(dtdDataSetWithAllTables)).contains(
				"<!ELEMENT dataset (\n" + "    FOLLOWER*,\n" + "    SEQUENCE*,\n" + "    TWEET*,\n" + "    USER*)>\n" + "\n"
						+ "<!ELEMENT FOLLOWER EMPTY>\n" + "<!ATTLIST FOLLOWER\n" + "    ID CDATA #REQUIRED\n"
						+ "    USER_ID CDATA #IMPLIED\n" + "    FOLLOWER_ID CDATA #IMPLIED\n" + ">\n" + "\n"
						+ "<!ELEMENT SEQUENCE EMPTY>\n" + "<!ATTLIST SEQUENCE\n" + "    SEQ_NAME CDATA #REQUIRED\n"
						+ "    SEQ_COUNT CDATA #IMPLIED\n" + ">\n" + "\n" + "<!ELEMENT TWEET EMPTY>\n" + "<!ATTLIST TWEET\n"
						+ "    ID CDATA #REQUIRED\n" + "    CONTENT CDATA #IMPLIED\n" + "    DATE CDATA #IMPLIED\n"
						+ "    LIKES CDATA #IMPLIED\n" + "    USER_ID CDATA #IMPLIED\n" + ">\n" + "\n" + "<!ELEMENT USER EMPTY>\n"
						+ "<!ATTLIST USER\n" + "    ID CDATA #REQUIRED\n" + "    NAME CDATA #IMPLIED\n" + ">\n" + "\n");
	}

	private static void assertXMLFileContentFiltered(String filename) {
		File xmlDataSet = new File(filename);
		assertThat(xmlDataSet).exists();

		assertThat(contentOf(xmlDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
				.doesNotContain("<USER ID=\"2\" NAME=\"@dbunit\"/>")
				.doesNotContain("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");
	}

	private static void assertDTDFileContentFiltered(String filename) {
		File dtdDataSet = new File(filename);
		assertThat(dtdDataSet).exists();

		assertThat(contentOf(dtdDataSet)).contains(
				"<!ELEMENT dataset (\n" + "    USER*)>\n" + "\n" + "<!ELEMENT USER EMPTY>\n" + "<!ATTLIST USER\n"
						+ "    ID CDATA #REQUIRED\n" + "    NAME CDATA #IMPLIED\n" + ">\n" + "\n");
	}

	private static void assertXMLFileContentFilteredIncludes(String filename) {
		File xmlDataSet = new File(filename);
		assertThat(xmlDataSet).exists();

		assertThat(contentOf(xmlDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
				.contains("<TWEET ID=\"abcdef12345\" CONTENT=\"dbunit rules!\"")
				.doesNotContain("<USER ID=\"2\" NAME=\"@dbunit\"/>")
				.doesNotContain("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>");
	}

	private static void assertDTDFileContentFilteredIncludes(String filename) {
		File dtdDataSet = new File(filename);
		assertThat(dtdDataSet).exists();

		assertThat(contentOf(dtdDataSet)).contains(
				"<!ELEMENT dataset (\n" + "    TWEET*,\n" + "    USER*)>\n" + "\n" + "<!ELEMENT TWEET EMPTY>\n"
						+ "<!ATTLIST TWEET\n" + "    ID CDATA #REQUIRED\n" + "    CONTENT CDATA #IMPLIED\n"
						+ "    DATE CDATA #IMPLIED\n" + "    LIKES CDATA #IMPLIED\n" + "    USER_ID CDATA #IMPLIED\n" + ">\n" + "\n"
						+ "<!ELEMENT USER EMPTY>\n" + "<!ATTLIST USER\n" + "    ID CDATA #REQUIRED\n" + "    NAME CDATA #IMPLIED\n"
						+ ">\n" + "\n");
	}

	private static void assertXMLFileContentDependent(String filename) {
		File xmlDataSet = new File(filename);
		assertThat(xmlDataSet).exists();

		assertThat(contentOf(xmlDataSet)).contains("<USER ID=\"1\" NAME=\"@realpestano\"/>")
				.contains("<USER ID=\"2\" NAME=\"@dbunit\"/>")
				.contains("<FOLLOWER ID=\"1\" USER_ID=\"1\" FOLLOWER_ID=\"2\"/>")
				.contains("<TWEET ID=\"abcdef12345\" CONTENT=\"dbunit rules!\"");
	}

	private static void assertDTDFileContentDependent(String filename) {
		File dtdDataSet = new File(filename);
		assertThat(dtdDataSet).exists();

		assertThat(contentOf(dtdDataSet)).contains(
				"<!ELEMENT dataset (\n" +
						"    USER*,\n" +
						"    FOLLOWER*,\n" +
						"    TWEET*)>\n" +
						"\n" +
						"<!ELEMENT USER EMPTY>\n" +
						"<!ATTLIST USER\n" +
						"    ID CDATA #REQUIRED\n" +
						"    NAME CDATA #IMPLIED\n" +
						">\n" +
						"\n" +
						"<!ELEMENT FOLLOWER EMPTY>\n" +
						"<!ATTLIST FOLLOWER\n" +
						"    ID CDATA #REQUIRED\n" +
						"    USER_ID CDATA #IMPLIED\n" +
						"    FOLLOWER_ID CDATA #IMPLIED\n" +
						">\n" +
						"\n" +
						"<!ELEMENT TWEET EMPTY>\n" +
						"<!ATTLIST TWEET\n" +
						"    ID CDATA #REQUIRED\n" +
						"    CONTENT CDATA #IMPLIED\n" +
						"    DATE CDATA #IMPLIED\n" +
						"    LIKES CDATA #IMPLIED\n" +
						"    USER_ID CDATA #IMPLIED\n" +
						">");
	}
}