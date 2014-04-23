/**
 * 
 */
package org.hsh.bfr.db.imports;


/**
 * 
 */

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.zip.CRC32;

import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellStyle;
import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;

/**
 * @author Armin
 *
 */
@SuppressWarnings("unused")
public class LieferkettenImporterEFSA extends FileFilter implements MyImporter {
  /**
  This is the one of the methods that is declared in 
  the abstract class
 */
	private HashMap<String, Integer> nodeIDs = null;
	private int maxNodeID = 0;
	private String theExcelFile = null;

	public void mergeIDs() throws IOException {
		if (theExcelFile != null) {
			HashMap<String, Integer> result = new HashMap<String, Integer>();
			FileInputStream is = new FileInputStream(theExcelFile);
		    POIFSFileSystem fs = new POIFSFileSystem(is);
		    HSSFWorkbook wb = new HSSFWorkbook(fs);

		    HSSFSheet defaultSheet = wb.getSheet("mergeList"); 		
			int numRows = defaultSheet.getLastRowNum() + 1;
	      	for (int i=1;i<numRows;i++) {
	      		try {
	          		//System.err.println(i);
	          		HSSFRow row = defaultSheet.getRow(i);
	    			if (row != null) {
	    				HSSFCell cell = row.getCell(0);
	    				Integer oldID = (int) cell.getNumericCellValue();
	    				cell = row.getCell(1);
	    				Integer newID = (int) cell.getNumericCellValue();
	    				DBKernel.mergeIDs(DBKernel.getDBConnection(), "Station", oldID, newID);
	    			}
	      		}
	      		catch (Exception e) {System.err.println(e.getMessage() + "\t" + i);}
	      	}
		}
	}

	public void importNodeIDs() {
		if (nodeIDs == null) {
		  	String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
		  	  JFileChooser fc = new JFileChooser(lastOutDir);
		  	  FileFilter ff = new XlsFileFilter();
		  	  fc.addChoosableFileFilter(ff);
		  	  fc.setFileFilter(ff);
		  	fc.setAcceptAllFileFilterUsed(false);
		  	fc.setMultiSelectionEnabled(false);
		  	fc.setDialogTitle("Import Node-Ids to use...");
				  int returnVal = fc.showOpenDialog(null);
				  if (returnVal == JFileChooser.APPROVE_OPTION) {
					  	File selectedSingleFile = fc.getSelectedFile();
				  		if (selectedSingleFile != null) {
							  //nodeIDs = loadExternalXLS4StationMapping("C:/Users/Armin/Desktop/AllKrisen/EFSA/nodes_ids.xls");
							  try {
								  theExcelFile = selectedSingleFile.getAbsolutePath();
								nodeIDs = loadExternalXLS4StationMapping(theExcelFile);
							}
							  catch (IOException e) {
								e.printStackTrace();
							}
				  		}
				  }
		}
	}
	public boolean accept(File f) {
	  if (f.isDirectory()) return true;
	
	  String extension = getExtension(f);
	  if ((extension.equals("xls"))) return true; 
	  return false;
	}
	  
	public String getDescription() {
	    return "New Lieferketten Datei (*.xls)";
	}
	
	private String getExtension(File f) {
	  String s = f.getName();
	  int i = s.lastIndexOf('.');
	  if (i > 0 &&  i < s.length() - 1) return s.substring(i+1).toLowerCase();
	  return "";
	}

	private HashMap<String, Integer> loadExternalXLS4StationMapping(String filename) throws IOException {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		FileInputStream is = new FileInputStream(filename);
	    POIFSFileSystem fs = new POIFSFileSystem(is);
	    HSSFWorkbook wb = new HSSFWorkbook(fs);

	    HSSFSheet defaultSheet = wb.getSheet("default"); 		
		int numRows = defaultSheet.getLastRowNum() + 1;
		Integer dbmid = DBKernel.getMaxID("Station");
		if (dbmid != null) maxNodeID = dbmid;
      	for (int i=1;i<numRows;i++) {
      		try {
          		//System.err.println(i);
          		HSSFRow row = defaultSheet.getRow(i);
    			if (row != null) {
    				HSSFCell cell = row.getCell(0); // ID
    				Integer id = (int) cell.getNumericCellValue();
    				if (id > maxNodeID) maxNodeID = id;
    				String val = "";
    				cell = row.getCell(1); // Name
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    				cell = row.getCell(2); // Strasse
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    				cell = row.getCell(3); // Hausnummer
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    				cell = row.getCell(4); // ZIP
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    				cell = row.getCell(5); // City
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    				cell = row.getCell(6); // County
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    				cell = row.getCell(7); // Country
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    				cell = row.getCell(8); // VAT
    				val += (cell == null ? "" : cell.getStringCellValue()) + ";;;";
    			    result.put(val, id);
    			}
      		}
      		catch (Exception e) {System.err.println(e.getMessage() + "\t" + i);}
      	}
      	return result;
	}
	private int[] doImportMaciel(HSSFWorkbook wb, JProgressBar progress, String efsaID) {
	    int numSuccess = 0;
	    int numFails = 0;
	    HSSFSheet transactionSheet = wb.getSheet("Receivers"); 
	    HSSFSheet businessSheet = wb.getSheet("Business_List"); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		progress.setMaximum(numRows);
      	progress.setValue(0);
	    HSSFRow busRow = getRow(businessSheet, efsaID, 0);
	  	  String nameLST = getStrVal(busRow.getCell(1));
	  	  String streetLST = getStrVal(busRow.getCell(2));
	  	  String streetNoLST = getStrVal(busRow.getCell(3), 10);
	  	  String zipLST = getStrVal(busRow.getCell(4), 10);
	  	  String cityLST = getStrVal(busRow.getCell(5));
	  	  String countyLST = getStrVal(busRow.getCell(6)); 
	  	  String countryLST = getStrVal(busRow.getCell(7));
	  	  String vatLST = getStrVal(busRow.getCell(8));
      	for (int i=2;i<numRows;i++) {
      		HSSFRow row = transactionSheet.getRow(i);
			if (row != null) {
			      String addressOther = getStrVal(row.getCell(7));
			      busRow = getRow(businessSheet, addressOther, 9);
			      if (busRow != null) {
			    	  String name = getStrVal(busRow.getCell(1)); //
			    	  String street = getStrVal(busRow.getCell(2)); //
			    	  String streetNo = getStrVal(busRow.getCell(3), 10); //
			    	  String zip = getStrVal(busRow.getCell(4), 10); //
			    	  String city = getStrVal(busRow.getCell(5)); //
			    	  String county = getStrVal(busRow.getCell(6)); 
			    	  String country = getStrVal(busRow.getCell(7)); // 
			    	  String vat = getStrVal(busRow.getCell(8)); //
			    	  getCharge_Lieferung(nameLST, streetLST, streetNoLST, zipLST, cityLST, countyLST, countryLST, null, vatLST,
			    			  null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			    			  name, street, streetNo, zip, city, county, country, null, vat,
			    			  "LSTZAK" + efsaID + "_" + (i+1), null, null, null, null, null);
			      }
			}
      }
      return new int[]{numSuccess, numFails};
	}
	private int[] doImportGaia(HSSFWorkbook wb, JProgressBar progress) {
	    int numSuccess = 0;
	    int numFails = 0;
	    HSSFSheet transactionSheet = wb.getSheet("Transactions"); 
	    HSSFSheet businessSheet = wb.getSheet("Business_List"); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		progress.setMaximum(numRows);
      	progress.setValue(0);
      	for (int i=1;i<numRows;i++) {
      		HSSFRow row = transactionSheet.getRow(i);
			if (row != null) {
			      String idRec = getStrVal(row.getCell(0));
			      String adressRec = getStrVal(row.getCell(1));
			      String countryRec = getStrVal(row.getCell(2));
			      String nameRec = adressRec;
			      String streetRec = null;
			      String streetNoRec = null;
			      String zipRec = null;
			      String cityRec = null;
			      String countyRec = null;
			      String vatRec = null;
			      HSSFRow busRow = getRow(businessSheet, idRec, 0);
			      if (busRow != null) {
				      nameRec = getStrVal(busRow.getCell(1)); //
				      streetRec = getStrVal(busRow.getCell(2)); //
				      streetNoRec = getStrVal(busRow.getCell(3), 10); //
				      zipRec = getStrVal(busRow.getCell(4), 10); //
				      cityRec = getStrVal(busRow.getCell(5)); //
				      countyRec = getStrVal(busRow.getCell(6)); 
				      countryRec = getStrVal(busRow.getCell(7)); // 
				      vatRec = getStrVal(busRow.getCell(8)); //
				      if (!adressRec.startsWith(nameRec)) {
				    	  //System.err.println("Id issue on recs... " + nameRec + " <> " + adressRec);
				      }
			      }
			      else if (idRec != null) {
			    	  System.err.println("business not there??? Row: " + (i+1) + "\tidReceived: " + idRec);
			      }
			      else {
			    	  System.err.println("idRec is null??? Row: " + (i+1) + "\t" + nameRec + (nameRec != null ? "" : " -> Station not defined"));
			      }
			      
			      String prodName = getStrVal(row.getCell(3));
			      String type = getStrVal(row.getCell(4));
			      if (type != null) prodName += "(" + type + ")";
			      			      
			      String idSup = getStrVal(row.getCell(5));
			      String adressSup = getStrVal(row.getCell(6));
			      String countrySup = getStrVal(row.getCell(7));
			      String nameSup = adressSup;
			      String streetSup = null;
			      String streetNoSup = null;
			      String zipSup = null;
			      String citySup = null;
			      String countySup = null;
			      String vatSup = null;
			      busRow = getRow(businessSheet, idSup, 0);
			      if (busRow != null) {
				      nameSup = getStrVal(busRow.getCell(1)); //
				      streetSup = getStrVal(busRow.getCell(2)); //
				      streetNoSup = getStrVal(busRow.getCell(3), 10); //
				      zipSup = getStrVal(busRow.getCell(4), 10); //
				      citySup = getStrVal(busRow.getCell(5)); //
				      countySup = getStrVal(busRow.getCell(6)); 
				      countrySup = getStrVal(busRow.getCell(7)); // 
				      vatSup = getStrVal(busRow.getCell(8)); //
				      if (!adressSup.startsWith(nameSup)) {
				    	  //System.err.println("Id issue on sups... Row: " + (i+1) + "\t" + nameSup + " <> " + adressSup);
				      }
			      }
			      else if (idSup != null) {
			    	  System.err.println("business not there??? Row: " + (i+1) + "\tidSupplier: " + idSup);
			      }
			      else {
			    	  System.err.println("idSup is null??? Row: " + (i+1) + "\t" + nameSup + (nameSup != null ? "" : " -> Station not defined"));
			      }

			      String anno = getStrVal(row.getCell(8)); // anno
			      String sitoweb = getStrVal(row.getCell(9)); // Sito Web
			      String oc = getStrVal(row.getCell(10)); // OriginCountry
			      String cqr = (anno == null ? "" : anno) + (sitoweb == null ? "" : "\t" + sitoweb);

			      String serial = "Gaia_" + (i+1);
			      Integer c1 = null;
			      if (nameSup != null && !nameSup.trim().isEmpty()) {
			    	  Integer[] c = getCharge_Lieferung(nameSup, streetSup, streetNoSup, zipSup, citySup, countySup, countrySup, null, vatSup,
			    			  prodName, null, null, null, null, null, null, null, null, null, oc, null, null, null, null, null, null,
			    			  nameRec, streetRec, streetNoRec, zipRec, cityRec, countyRec, countryRec, null, vatRec,
			    			  serial, cqr, null, null, null, null);
			    	  if (c != null) c1 = c[2];
			      }
			      if (c1 == null) {
			      	System.err.println("Fehlerchenchen_1!! Row: " + (i+1));
			      	numFails++;
			      }
      	}
      }
      return new int[]{numSuccess, numFails};
	}
	private int[] doImportStandard(HSSFWorkbook wb, JProgressBar progress) {
	    int numSuccess = 0;
	    int numFails = 0;
	    HSSFSheet transactionSheet = wb.getSheet("Transactions"); 
	    HSSFSheet businessSheet = wb.getSheet("Business_List"); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		progress.setMaximum(numRows);
      	progress.setValue(0);
			      
      	for (int i=1;i<numRows;i++) {
      		HSSFRow row = transactionSheet.getRow(i);
			if (row != null) {
			      String serial = getStrVal(row.getCell(0)); // Serial_number
			      //String BL0 = getStrVal(row.getCell(1)); // Contact_Region
			      //String KP = getStrVal(row.getCell(2)); // Contact_person
			      
			      String idRec = getStrVal(row.getCell(3)); // ID_Address
			      String adressRec = getStrVal(row.getCell(4)); // Address
			      if ((serial == null || serial.trim().isEmpty()) && (adressRec == null || adressRec.trim().isEmpty())) {
				      	continue;//break;
				      }
			      String activityRec = getStrVal(row.getCell(5)); // Activity				      
			      String nameRec = adressRec;
			      String streetRec = null;
			      String streetNoRec = null;
			      String zipRec = null;
			      String cityRec = null;
			      String countyRec = null;
			      String countryRec = null;
			      String vatRec = null;
			      HSSFRow busRow = getRow(businessSheet, idRec, 0);
			      if (busRow != null) {
				      nameRec = getStrVal(busRow.getCell(1)); //
				      streetRec = getStrVal(busRow.getCell(2)); //
				      streetNoRec = getStrVal(busRow.getCell(3), 10); //
				      zipRec = getStrVal(busRow.getCell(4), 10); //
				      cityRec = getStrVal(busRow.getCell(5)); //
				      countyRec = getStrVal(busRow.getCell(6)); 
				      countryRec = getStrVal(busRow.getCell(7)); // 
				      vatRec = getStrVal(busRow.getCell(8)); //
				      if (!adressRec.toUpperCase().startsWith(nameRec.toUpperCase())) {
				    	  System.err.println("Id issue on recs...Row: " + (i+1) + "\t" + nameRec + " <> " + adressRec);
				      }
			      }
			      else if (idRec != null) {
			    	  System.err.println("business not there??? Row: " + (i+1) + "\tidReceived: " + idRec);
			      }
			      else {
			    	  System.err.println("idRec is null??? Row: " + (i+1) + "\t" + adressRec + (adressRec != null ? "" : " -> Station not defined"));
			      }
			      
			      String prodNameOut = getStrVal(row.getCell(6)); // ProductName
			      String prodNumOut = getStrVal(row.getCell(7)); // ProductNo
			      String dayOut = getStrVal(row.getCell(8)); // Day
			      String monthOut = getStrVal(row.getCell(9)); // Month
			      String yearOut = getStrVal(row.getCell(10)); // Year
			      String amountKG_Out = getStrVal(row.getCell(11)); // amountKG
			      String typePUOut = getStrVal(row.getCell(12)); // typePU
			      String numPUOut = getStrVal(row.getCell(13)); // numPU
			      String lotNo_Out = getStrVal(row.getCell(14)); // 
			      String dayMHDOut = getStrVal(row.getCell(15)); 
			      String monthMHDOut = getStrVal(row.getCell(16)); 
			      String yearMHDOut = getStrVal(row.getCell(17)); // 
			      String dayPDOut = getStrVal(row.getCell(18)); 
			      String monthPDOut = getStrVal(row.getCell(19)); 
			      String yearPDOut = getStrVal(row.getCell(20)); 
			      //Date dateOut = getDate(dayOut, monthOut, yearOut);
			      //Date dateMHDOut = getDate(dayMHDOut, monthMHDOut, yearMHDOut);
			      //Date datePDOut = getDate(dayPDOut, monthPDOut, yearPDOut);
			      
			      String idInsp = getStrVal(row.getCell(21)); // ID_Address
			      String adressInsp = getStrVal(row.getCell(22)); // Address
			      String activityInsp = getStrVal(row.getCell(23)); // Activity
			      String nameInsp = adressInsp;
			      String streetInsp = null;
			      String streetNoInsp = null;
			      String zipInsp = null;
			      String cityInsp = null;
			      String countyInsp = null;
			      String countryInsp = null;
			      String vatInsp = null;
			      busRow = getRow(businessSheet, idInsp, 0);
			      if (busRow != null) {
				      nameInsp = getStrVal(busRow.getCell(1)); //
				      streetInsp = getStrVal(busRow.getCell(2)); //
				      streetNoInsp = getStrVal(busRow.getCell(3), 10); //
				      zipInsp = getStrVal(busRow.getCell(4), 10); //
				      cityInsp = getStrVal(busRow.getCell(5)); //
				      countyInsp = getStrVal(busRow.getCell(6)); 
				      countryInsp = getStrVal(busRow.getCell(7)); // 
				      vatInsp = getStrVal(busRow.getCell(8)); //
				      if (!adressInsp.toUpperCase().startsWith(nameInsp.toUpperCase())) {
				    	  System.err.println("Id issue on insps...Row: " + (i+1) + "\t" + nameInsp + " <> " + adressInsp);
				      }
			      }
			      else if (idInsp != null) {
			    	  System.err.println("business not there??? Row: " + (i+1) + "\tidInspected: " + idInsp);
			      }
			      else {
			    	  System.err.println("idInsp is null??? Row: " + (i+1) + "\t" + adressInsp + (adressInsp != null ? "" : " -> Station not defined"));
			      }

			      String prodNameIn = getStrVal(row.getCell(24)); // ProductName
			      String prodNumIn = getStrVal(row.getCell(25)); // ProductNo
			      String dayIn = getStrVal(row.getCell(26)); // Day
			      String monthIn = getStrVal(row.getCell(27)); // Month
			      String yearIn = getStrVal(row.getCell(28)); // Year
			      String amountKG_In = getStrVal(row.getCell(29)); // amountKG
			      String typePUIn = getStrVal(row.getCell(30)); // typePU
			      String numPUIn = getStrVal(row.getCell(31)); // numPU
			      String lotNo_In = getStrVal(row.getCell(32)); // 
			      String dayMHDIn = getStrVal(row.getCell(33)); 
			      String monthMHDIn = getStrVal(row.getCell(34)); 
			      String yearMHDIn = getStrVal(row.getCell(35)); // 
			      String dayPDIn = getStrVal(row.getCell(36)); 
			      String monthPDIn = getStrVal(row.getCell(37)); 
			      String yearPDIn = getStrVal(row.getCell(38)); 
			      //Date dateIn = getDate(dayIn, monthIn, yearIn);
			      //Date dateMHDIn = getDate(dayMHDIn, monthMHDIn, yearMHDIn);
			      //Date datePDIn = getDate(dayPDIn, monthPDIn, yearPDIn);
			      
			      String idSup = getStrVal(row.getCell(39)); // ID_Address
			      String adressSup = getStrVal(row.getCell(40)); // Address
			      String activitySup = getStrVal(row.getCell(41)); // Activity
			      String nameSup = adressSup;
			      String streetSup = null;
			      String streetNoSup = null;
			      String zipSup = null;
			      String citySup = null;
			      String countySup = null;
			      String countrySup = null;
			      String vatSup = null;
			      busRow = getRow(businessSheet, idSup, 0);
			      if (busRow != null) {
				      nameSup = getStrVal(busRow.getCell(1)); //
				      streetSup = getStrVal(busRow.getCell(2)); //
				      streetNoSup = getStrVal(busRow.getCell(3), 10); //
				      zipSup = getStrVal(busRow.getCell(4), 10); //
				      citySup = getStrVal(busRow.getCell(5)); //
				      countySup = getStrVal(busRow.getCell(6)); 
				      countrySup = getStrVal(busRow.getCell(7)); // 
				      vatSup = getStrVal(busRow.getCell(8)); //
				      if (!adressSup.toUpperCase().startsWith(nameSup.toUpperCase())) {
				    	  System.err.println("Id issue on sups...Row: " + (i+1) + "\t" + nameSup + " <> " + adressSup);
				      }
			      }
			      else if (idSup != null) {
			    	  System.err.println("business not there??? Row: " + (i+1) + "\tidSupplier: " + idSup);
			      }
			      else {
			    	  System.err.println("idSup is null??? Row: " + (i+1) + "\t" + adressSup + (adressSup != null ? "" : " -> Station not defined"));
			      }

			      String ec = getStrVal(row.getCell(42)); // EndChain
			      String ece = getStrVal(row.getCell(43)); // Explanation_EndChain
			      String oc = getStrVal(row.getCell(44)); // OriginCountry
			      String cqr = getStrVal(row.getCell(45)); // Contact_Questions_Remarks
			      String ft = getStrVal(row.getCell(46)); // Further_Traceback
			      String ms = getStrVal(row.getCell(47)); // MicrobiologicalSample

			      //if (amountKG_Out != null && amountKG_In != null && Integer.parseInt(amountKG_Out) > Integer.parseInt(amountKG_In)) System.err.println("amountOut > aomountIn!!! Row " + i + "; amountKG_Out: " + amountKG_Out + "; amountKG_In: " + amountKG_In);
			      if (is1SurelyNewer(dayIn, monthIn, yearIn, dayOut, monthOut, yearOut)) System.err.println("- Dates not in temporal order, dateOut < dateIn!!! Row: " + (i+1) + "; dateOut: " + sdfFormat(dayOut, monthOut, yearOut) + "; dateIn: " + sdfFormat(dayIn, monthIn, yearIn));

			      Integer c1 = null;
			      Integer c2 = null;
			      if (nameInsp != null && !nameInsp.trim().isEmpty()) {
			    	  Integer[] c = getCharge_Lieferung(nameInsp, streetInsp, streetNoInsp, zipInsp, cityInsp, countyInsp, countryInsp, activityInsp, vatInsp,
			    			  prodNameOut, prodNumOut, null, lotNo_Out, dayMHDOut, monthMHDOut, yearMHDOut, dayPDOut, monthPDOut, yearPDOut, oc, dayOut, monthOut, yearOut, amountKG_Out, typePUOut, numPUOut,
			    			  nameRec, streetRec, streetNoRec, zipRec, cityRec, countyRec, countryRec, activityRec, vatRec,
			    			  serial, cqr, null, null, null, null);
			    	  if (c != null) c1 = c[2];
			      }
			      if (nameSup != null && !nameSup.trim().isEmpty()) {
			    	  Integer[] c = getCharge_Lieferung(nameSup, streetSup, streetNoSup, zipSup, citySup, countySup, countrySup, activitySup, vatSup,
				    		  prodNameIn, prodNumIn, null, lotNo_In, dayMHDIn, monthMHDIn, yearMHDIn, dayPDIn, monthPDIn, yearPDIn, oc, dayIn, monthIn, yearIn, amountKG_In, typePUIn, numPUIn,
				    		  nameInsp, streetInsp, streetNoInsp, zipInsp, cityInsp, countyInsp, countryInsp, activityInsp, vatInsp,
				    		  serial, cqr, ec, ece, ft, ms);
			    	  if (c != null) c2 = c[3];
			      }
			      if (c1 == null) {
			      	System.err.println("Fehlerchenchen_1!! Row: " + (i+1));
			      	numFails++;
			      }
			      else if (c2 == null) {
			      	System.err.println("Fehlerchenchen_2!! E.g. Station not defined? Row: " + (i+1));
			      	/*
			      	getCharge_Lieferung(nameSup, streetSup, streetNoSup, zipSup, citySup, countySup, countrySup, activitySup, vatSup,
				    		prodNameIn, prodNumIn, lotNo_In, dateMHDIn, datePDIn, oc, dateIn, amountKG_In, typePUIn, numPUIn,
			      			nameSup, streetSup, streetNoSup, zipSup, citySup, countySup, countrySup, activityInsp, vatInsp,
			      			comment, false);
			      		*/	
			      	numFails++;
			      }
			      else {
			      	if (c2 != null) {
				      	if (getID("ChargenVerbindungen",
	    							new String[]{"Zutat","Produkt"},
	    							new String[]{c2.toString(), c1.toString()},
	    							null,null) == null) {
					      	System.err.println("Fehlerchenchen_4!! Row: " + (i+1));
					      	numFails++;
					      }
					      else {
					      	numSuccess++;
					      }
				      }
			    }
      	}
      }
      	return new int[]{numSuccess, numFails};
    }
	private int[] doImportNewFormat(HSSFWorkbook wb, JProgressBar progress) {
	    int numSuccess = 0;
	    int numFails = 0;
	    HSSFSheet transactionSheet = wb.getSheet("NewTransactions"); 
	    HSSFSheet businessSheet = wb.getSheet("Business_List"); 
	    HashMap<String, Integer[]> storedIDs = new HashMap<String, Integer[]>(); 
	    HashMap<String, String> storedBackS = new HashMap<String, String>(); 
		int numRows = transactionSheet.getLastRowNum() + 1;
		progress.setMaximum(numRows);
      	progress.setValue(0);
			      
      	for (int i=1;i<numRows;i++) {
      		HSSFRow row = transactionSheet.getRow(i);
			if (row != null) {
			      String backSerial = getStrVal(row.getCell(0), 16383);
			      String serial = getStrVal(row.getCell(1));
			      
			      if ((serial == null || serial.trim().isEmpty())) {
				      	continue;//break;
				      }
			      
			      String adressSup = getStrVal(row.getCell(2));
			      String nameSup = null;
			      String streetSup = null;
			      String streetNoSup = null;
			      String zipSup = null;
			      String citySup = null;
			      String countySup = null;
			      String countrySup = null;
			      String vatSup = null;
			      HSSFRow busRow = getRow(businessSheet, adressSup, 9);
			      if (busRow != null) {
				      nameSup = getStrVal(busRow.getCell(1));
				      streetSup = getStrVal(busRow.getCell(2));
				      streetNoSup = getStrVal(busRow.getCell(3), 10);
				      zipSup = getStrVal(busRow.getCell(4), 10);
				      citySup = getStrVal(busRow.getCell(5));
				      countySup = getStrVal(busRow.getCell(6)); 
				      countrySup = getStrVal(busRow.getCell(7));
				      vatSup = getStrVal(busRow.getCell(8));
			      }
			      else if (adressSup != null) {
			    	  System.err.println("business not there??? Row: " + (i+1) + "\tadressSup: " + adressSup);
			      }
			      else {
			    	  System.err.println("adressSup is null??? Row: " + (i+1) + "\t" + adressSup + (adressSup != null ? "" : " -> Station not defined"));
			      }
			      
			      String prodName = getStrVal(row.getCell(3));
			      String prodNum = getStrVal(row.getCell(4));
			      String prodTreatment = getStrVal(row.getCell(5));

			      String lotNo_ = getStrVal(row.getCell(6));
			      String dayPD = getStrVal(row.getCell(7)); 
			      String monthPD = getStrVal(row.getCell(8)); 
			      String yearPD = getStrVal(row.getCell(9)); 
			      String dayMHD = getStrVal(row.getCell(10)); 
			      String monthMHD = getStrVal(row.getCell(11)); 
			      String yearMHD = getStrVal(row.getCell(12));

			      String day = getStrVal(row.getCell(13));
			      String month = getStrVal(row.getCell(14));
			      String year = getStrVal(row.getCell(15));
			      String amountKG_ = getStrVal(row.getCell(16));
			      String numPU = getStrVal(row.getCell(17));
			      String typePU = getStrVal(row.getCell(18));
			      
			      String adressRec = getStrVal(row.getCell(19));
			      String nameRec = adressRec;
			      String streetRec = null;
			      String streetNoRec = null;
			      String zipRec = null;
			      String cityRec = null;
			      String countyRec = null;
			      String countryRec = null;
			      String vatRec = null;
			      busRow = getRow(businessSheet, adressRec, 9);
			      if (busRow != null) {
				      nameRec = getStrVal(busRow.getCell(1));
				      streetRec = getStrVal(busRow.getCell(2));
				      streetNoRec = getStrVal(busRow.getCell(3), 10);
				      zipRec = getStrVal(busRow.getCell(4), 10);
				      cityRec = getStrVal(busRow.getCell(5));
				      countyRec = getStrVal(busRow.getCell(6)); 
				      countryRec = getStrVal(busRow.getCell(7));
				      vatRec = getStrVal(busRow.getCell(8));
			      }
			      else if (adressRec != null) {
			    	  System.err.println("business not there??? Row: " + (i+1) + "\tadressRec: " + adressRec);
			      }
			      else {
			    	  System.err.println("adressRec is null??? Row: " + (i+1) + "\t" + adressRec + (adressRec != null ? "" : " -> Station not defined"));
			      }

			      String ec = getStrVal(row.getCell(21)); // EndChain
			      String ece = getStrVal(row.getCell(22)); // Explanation_EndChain
			      String oc = getStrVal(row.getCell(23)); // OriginCountry
			      String cqr = getStrVal(row.getCell(24), 16383); // Contact_Questions_Remarks
			      String ft = getStrVal(row.getCell(25)); // Further_Traceback
			      String ms = getStrVal(row.getCell(26)); // MicrobiologicalSample

			      Integer[] c = null;
			      if (nameSup != null && !nameSup.trim().isEmpty()) {
			    	  c = getCharge_Lieferung(nameSup, streetSup, streetNoSup, zipSup, citySup, countySup, countrySup, null, vatSup,
				    		  prodName, prodNum, prodTreatment, lotNo_, dayMHD, monthMHD, yearMHD, dayPD, monthPD, yearPD, oc, day, month, year, amountKG_, typePU, numPU,
				    		  nameRec, streetRec, streetNoRec, zipRec, cityRec, countyRec, countryRec, null, vatRec,
				    		  serial, cqr, ec, ece, ft, ms);
				      storedIDs.put(serial, c);
				      storedBackS.put(serial, backSerial);
			      }
			}
      	}
      	for (String serial : storedIDs.keySet()) {
      		Integer[] c = storedIDs.get(serial);
      		String backSerials = storedBackS.get(serial);
      		if (backSerials != null && !backSerials.isEmpty()) {
          		StringTokenizer tok = new StringTokenizer(backSerials, "\n");
          		while (tok.hasMoreTokens()) {
          			String backS = tok.nextToken();
          			if (storedIDs.containsKey(backS)) {
              			Integer[] cBack = storedIDs.get(backS);
              			if (is1SurelyNewer(cBack[3], c[3])) {
              				System.err.println("- Dates not in temporal order, dateOut < dateIn!!! Serial: " + serial + "; PreviousSerial: " + backS);
              			}
              	      if (c[2] == null) {
              	      	System.err.println("Fehlerchenchen_1!! Serial: " + backS);
              	      	numFails++;
              	      }
              	      else if (cBack[3] == null) {
              	      	System.err.println("Fehlerchenchen_2!! E.g. Station not defined? Serial: " + serial);
              	      	numFails++;
              	      }
              	      else if (cBack[4].intValue() != c[0].intValue()) {
              	    	System.err.println("Fehlerchenchen_3!! Recipient and Supplier different... Serial: " + serial);
              	      }
            	      else {
      			      	if (getID("ChargenVerbindungen",
      								new String[]{"Zutat","Produkt"},
      								new String[]{cBack[3].toString(), c[2].toString()},
      								null,null) == null) {
      				      	System.err.println("Fehlerchenchen_4!! Serial/PreviousSerial: " + serial + " / " + backS);
      				      	numFails++;
      				      }
      				      else {
      				      	numSuccess++;
      				      }
            		    }
          			}
          			else {
          				System.err.println("backSerial not there..." + backS);
          			}
          		}
      		}      		
      	}
	      return new int[]{numSuccess, numFails};
    }
	private void transformFormat(HSSFWorkbook wb, HSSFWorkbook wbNew) {
	    HSSFSheet transactionSheet = wb.getSheet("Transactions"); 
	    HSSFSheet businessSheet = wb.getSheet("Business_List"); 
		int numRows = transactionSheet.getLastRowNum() + 1;
	    HSSFSheet transactionSheetNew = wbNew.getSheet("NewTransactions"); 
	    HSSFRow newRow;
	    HSSFCell newCell;
	    HSSFSheet lookupNew = wbNew.getSheet("NewLookUp"); 
	    HashMap<Long, HSSFRow> storedRows = new HashMap<Long, HSSFRow>(); 
	    HashMap<Long, String> storedSerials = new HashMap<Long, String>(); 
	    CRC32 crc32 = new CRC32();
  	    CellStyle cs = wbNew.createCellStyle();
  	    cs.setWrapText(true);
			      
	    int newRowLfd=0;
	    int i=1;
      	for (;i<numRows;i++) {
      		HSSFRow row = transactionSheet.getRow(i);
			if (row != null) {
			      String serial = getStrVal(row.getCell(0)); // Serial_number
			      String contactPerson = getStrVal(row.getCell(2)); // person
			      String adressRec = getStrVal(row.getCell(4)); // Address
			      if ((serial == null || serial.trim().isEmpty())) {
			    	  if (contactPerson != null && !contactPerson.isEmpty() || adressRec != null && !adressRec.isEmpty()) {
			    		  System.err.println("serial is seriously null... " + (i+1));
			    	  }
			      }
			      else {
				      int index = serial.lastIndexOf("_");
				      if (index <= 0) {
				    	  System.err.println("index error ... no '_' there... " + (i+1));
				      }
				      serial = serial.substring(0, index) + "_" + (i + 1);

				      HSSFRow busRow = getRow(businessSheet, adressRec, 9);
				      if (busRow == null) {
				    	  System.err.println("Id issue on recs...Row: " + (i+1) + "\t" + adressRec);
				      }
				      else {
					      String s1 = getStrVal(busRow.getCell(1)); if (s1 == null) s1 = "";
					      String s2 = getStrVal(busRow.getCell(2)); if (s2 == null) s2 = "";
					      String s3 = getStrVal(busRow.getCell(3), 10); if (s3 == null) s3 = "";
					      String s4 = getStrVal(busRow.getCell(5)); if (s4 == null) s4 = "";
					      String s5 = getStrVal(busRow.getCell(7)); if (s5 == null) s5 = "";
				    	  adressRec = s1 + ", " + s2 + " " + s3 + ", " + s4 + ", " + s5; // =B3&", "&C3&" "&D3&", "&F3&", "&H3
				      }

				      String prodNameOut = getStrVal(row.getCell(6)); // ProductName
				      String prodNumOut = getStrVal(row.getCell(7)); // ProductNo
				      String dayOut = getStrVal(row.getCell(8)); // Day
				      String monthOut = getStrVal(row.getCell(9)); // Month
				      String yearOut = getStrVal(row.getCell(10)); // Year
				      String amountKG_Out = getStrVal(row.getCell(11)); // amountKG
				      String typePUOut = getStrVal(row.getCell(12)); // typePU
				      String numPUOut = getStrVal(row.getCell(13)); // numPU
				      String lotNo_Out = getStrVal(row.getCell(14)); // 
				      String dayMHDOut = getStrVal(row.getCell(15)); 
				      String monthMHDOut = getStrVal(row.getCell(16)); 
				      String yearMHDOut = getStrVal(row.getCell(17)); // 
				      String dayPDOut = getStrVal(row.getCell(18)); 
				      String monthPDOut = getStrVal(row.getCell(19)); 
				      String yearPDOut = getStrVal(row.getCell(20)); 
				      
				      String adressInsp = getStrVal(row.getCell(22)); // Address
				      String activityInsp = getStrVal(row.getCell(23)); // Activity
				      busRow = getRow(businessSheet, adressInsp, 9);
				      if (busRow == null) {
				    	  System.err.println("Id issue on insps...Row: " + (i+1) + "\t" + adressInsp);
				      }
				      else {
					      String s1 = getStrVal(busRow.getCell(1)); if (s1 == null) s1 = "";
					      String s2 = getStrVal(busRow.getCell(2)); if (s2 == null) s2 = "";
					      String s3 = getStrVal(busRow.getCell(3), 10); if (s3 == null) s3 = "";
					      String s4 = getStrVal(busRow.getCell(5)); if (s4 == null) s4 = "";
					      String s5 = getStrVal(busRow.getCell(7)); if (s5 == null) s5 = "";
					      adressInsp = s1 + ", " + s2 + " " + s3 + ", " + s4 + ", " + s5; // =B3&", "&C3&" "&D3&", "&F3&", "&H3
				      }

				      String prodNameIn = getStrVal(row.getCell(24)); // ProductName
				      String prodNumIn = getStrVal(row.getCell(25)); // ProductNo
				      String dayIn = getStrVal(row.getCell(26)); // Day
				      String monthIn = getStrVal(row.getCell(27)); // Month
				      String yearIn = getStrVal(row.getCell(28)); // Year
				      String amountKG_In = getStrVal(row.getCell(29)); // amountKG
				      String typePUIn = getStrVal(row.getCell(30)); // typePU
				      String numPUIn = getStrVal(row.getCell(31)); // numPU
				      String lotNo_In = getStrVal(row.getCell(32)); // 
				      String dayMHDIn = getStrVal(row.getCell(33)); 
				      String monthMHDIn = getStrVal(row.getCell(34)); 
				      String yearMHDIn = getStrVal(row.getCell(35)); // 
				      String dayPDIn = getStrVal(row.getCell(36)); 
				      String monthPDIn = getStrVal(row.getCell(37)); 
				      String yearPDIn = getStrVal(row.getCell(38)); 
				      
				      String adressSup = getStrVal(row.getCell(40)); // Address
				      String activitySup = getStrVal(row.getCell(41)); // Activity
				      busRow = getRow(businessSheet, adressSup, 9);
				      if (busRow == null) {
				    	  System.err.println("Id issue on susps...Row: " + (i+1) + "\t" + adressSup);
				      }
				      else {
					      String s1 = getStrVal(busRow.getCell(1)); if (s1 == null) s1 = "";
					      String s2 = getStrVal(busRow.getCell(2)); if (s2 == null) s2 = "";
					      String s3 = getStrVal(busRow.getCell(3), 10); if (s3 == null) s3 = "";
					      String s4 = getStrVal(busRow.getCell(5)); if (s4 == null) s4 = "";
					      String s5 = getStrVal(busRow.getCell(7)); if (s5 == null) s5 = "";
					      adressSup = s1 + ", " + s2 + " " + s3 + ", " + s4 + ", " + s5; // =B3&", "&C3&" "&D3&", "&F3&", "&H3
				      }

				      String ec = getStrVal(row.getCell(42)); // EndChain
				      String ece = getStrVal(row.getCell(43)); // Explanation_EndChain
				      String oc = getStrVal(row.getCell(44)); // OriginCountry
				      String cqr = getStrVal(row.getCell(45)); // Contact_Questions_Remarks
				      String ft = getStrVal(row.getCell(46)); // Further_Traceback
				      String ms = getStrVal(row.getCell(47)); // MicrobiologicalSample

				      busRow = getRow(lookupNew, activityInsp, 9);
				      String treatmentIn = null, treatmentOut = null;
				      if (busRow != null) treatmentOut = busRow.getCell(13).getStringCellValue();
				      busRow = getRow(lookupNew, activitySup, 9);
				      if (busRow != null) treatmentIn = busRow.getCell(13).getStringCellValue();
				      
				      String sOut = adressInsp + "_" + prodNameOut + "_" + prodNumOut + "_" + lotNo_Out + "_" + dayPDOut + "_" +
				    		  monthPDOut + "_" + yearPDOut + "_" + dayMHDOut + "_" + monthMHDOut + "_" + yearMHDOut + "_" + dayOut + "_" + monthOut + "_" +
				    		  yearOut + "_" + amountKG_Out + "_" + numPUOut + "_" + typePUOut + "_" + adressRec;
				      crc32.reset();
				      crc32.update(sOut.getBytes());
				      long crc32Out = crc32.getValue();
				      //System.err.println(crc32Out + " -> " + sOut);
				      String sIn = adressSup + "_" + prodNameIn + "_" + prodNumIn + "_" + lotNo_In + "_" + dayPDIn + "_" +
				    		  monthPDIn + "_" + yearPDIn + "_" + dayMHDIn + "_" + monthMHDIn + "_" + yearMHDIn + "_" + dayIn + "_" + monthIn + "_" +
				    		  yearIn + "_" + amountKG_In + "_" + numPUIn + "_" + typePUIn + "_" + adressInsp;
				      crc32.reset();
				      crc32.update(sIn.getBytes());
				      long crc32In = crc32.getValue();
				      //System.err.println(crc32In + " -> " + sIn);				    	  
				      
				      String backSerial = serial + ".1";
					  if (storedRows.containsKey(crc32In)) {
						  //HSSFRow r = storedRows.get(crc32In); backSerial = r.getCell(1).getStringCellValue();
						  backSerial = storedSerials.get(crc32In);
					  }
					  
				      if (storedRows.containsKey(crc32Out)) {
				    	  HSSFRow r = storedRows.get(crc32Out);
				    	  HSSFCell c = r.getCell(0);
				    	  if (c == null) {c = r.createCell(0); c.setCellStyle(cs); c.setCellValue(backSerial);}
				    	  else add2Cell(c, backSerial);
				    	  add2Cell(r.getCell(20), contactPerson);
				    	  add2Cell(r.getCell(23), oc);
				    	  add2Cell(r.getCell(24), cqr);
				    	  add2Cell(r.getCell(26), ms);
				      }
				      else {
				    	  newRowLfd++; newRow = transactionSheetNew.createRow(newRowLfd);
						  newCell = newRow.createCell(0, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(backSerial);
						  newCell = newRow.createCell(1, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(serial + ".0");
						  newCell = newRow.createCell(2, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(adressInsp);
						  newCell = newRow.createCell(3, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(prodNameOut);
						  newCell = newRow.createCell(4, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(prodNumOut);
						  if (treatmentOut != null) {newCell = newRow.createCell(5, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(treatmentOut);}
						  newCell = newRow.createCell(6, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(lotNo_Out);
						  newCell = newRow.createCell(7, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(dayPDOut);
						  newCell = newRow.createCell(8, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(monthPDOut);
						  newCell = newRow.createCell(9, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(yearPDOut);
						  newCell = newRow.createCell(10, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(dayMHDOut);
						  newCell = newRow.createCell(11, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(monthMHDOut);
						  newCell = newRow.createCell(12, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(yearMHDOut);
						  newCell = newRow.createCell(13, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(dayOut);
						  newCell = newRow.createCell(14, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(monthOut);
						  newCell = newRow.createCell(15, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(yearOut);
						  newCell = newRow.createCell(16, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(amountKG_Out);
						  newCell = newRow.createCell(17, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(numPUOut);
						  newCell = newRow.createCell(18, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(typePUOut);
						  newCell = newRow.createCell(19, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(adressRec);
						  newCell = newRow.createCell(20, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(contactPerson);
						  newCell = newRow.createCell(23, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(oc);
						  newCell = newRow.createCell(24, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(cqr);
						  newCell = newRow.createCell(26, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(ms);
					      storedRows.put(crc32Out, newRow);
					      storedSerials.put(crc32Out, serial + ".0");
				      }

				      if (storedRows.containsKey(crc32In)) {
				    	  HSSFRow r = storedRows.get(crc32In);
				    	  add2Cell(r.getCell(20), contactPerson);
				    	  add2Cell(r.getCell(21), ec);
				    	  add2Cell(r.getCell(22), ece);
				    	  add2Cell(r.getCell(23), oc);
				    	  add2Cell(r.getCell(24), cqr);
				    	  add2Cell(r.getCell(25), ft);
				    	  add2Cell(r.getCell(26), ms);
				      }
				      else {
					      newRowLfd++; newRow = transactionSheetNew.createRow(newRowLfd);
						  newCell = newRow.createCell(1, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(serial + ".1");
						  newCell = newRow.createCell(2, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(adressSup);
						  newCell = newRow.createCell(3, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(prodNameIn);
						  newCell = newRow.createCell(4, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(prodNumIn);
						  if (treatmentIn != null) {newCell = newRow.createCell(5, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(treatmentIn);}
						  newCell = newRow.createCell(6, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(lotNo_In);
						  newCell = newRow.createCell(7, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(dayPDIn);
						  newCell = newRow.createCell(8, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(monthPDIn);
						  newCell = newRow.createCell(9, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(yearPDIn);
						  newCell = newRow.createCell(10, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(dayMHDIn);
						  newCell = newRow.createCell(11, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(monthMHDIn);
						  newCell = newRow.createCell(12, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(yearMHDIn);
						  newCell = newRow.createCell(13, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(dayIn);
						  newCell = newRow.createCell(14, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(monthIn);
						  newCell = newRow.createCell(15, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(yearIn);
						  newCell = newRow.createCell(16, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(amountKG_In);
						  newCell = newRow.createCell(17, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(numPUIn);
						  newCell = newRow.createCell(18, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(typePUIn);
						  newCell = newRow.createCell(19, HSSFCell.CELL_TYPE_STRING); newCell.setCellValue(adressInsp);
						  newCell = newRow.createCell(20, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(contactPerson);
						  newCell = newRow.createCell(21, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(ec);
						  newCell = newRow.createCell(22, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(ece);
						  newCell = newRow.createCell(23, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(oc);
						  newCell = newRow.createCell(24, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(cqr);
						  newCell = newRow.createCell(25, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(ft);
						  newCell = newRow.createCell(26, HSSFCell.CELL_TYPE_STRING); newCell.setCellStyle(cs); newCell.setCellValue(ms);
					      storedRows.put(crc32In, newRow);
					      storedSerials.put(crc32In, serial + ".1");
				      }
			      }
			}
      }
      	System.err.println("last row: " + i);
    }
	private void add2Cell(HSSFCell c, String value) {
		if (c != null) {
			String ts = c.getStringCellValue();
			if (ts != null && value != null && ts.indexOf(value) < 0) c.setCellValue(ts + "\n" + value);
			//if (ts.length() > 5000) System.err.println(ts.length());
		}
	}
	public void doImport(final String filename, final JProgressBar progress, final boolean showResults) {
	  	Runnable runnable = new Runnable() {
	      public void run() {
	    	  System.err.println("Importing " + filename);
			  try {
	      		if (progress != null) {
	      			progress.setVisible(true);
	      			progress.setStringPainted(true);
	      			progress.setString("Importiere Lieferketten Datei...");
	      			progress.setMinimum(0);
	      		}
	
	      		InputStream is = null;
			    if (filename.startsWith("http://")) {
			    	URL url = new URL(filename);
			    	URLConnection uc = url.openConnection();
			    	is = uc.getInputStream();
			    }
			    else if (filename.startsWith("/org/hsh/bfr/db/res/")) {
					is = getClass().getResourceAsStream(filename);		    		
			    }
			    else {
			    	is = new FileInputStream(filename);
			    }
			
			    POIFSFileSystem fs = new POIFSFileSystem(is);
			    HSSFWorkbook wb = new HSSFWorkbook(fs);
	
			    boolean transformFormat = false;
			    int[] nsf = new int[2];
			    if (filename.endsWith("LST_partners.xls")) {
			    	if (transformFormat) ;
			    	else nsf = doImportMaciel(wb, progress, "98"); // 98: LST
			    }
			    else if (filename.endsWith("ZAK_partners.xls")) {
			    	if (transformFormat) ;
			    	else nsf = doImportMaciel(wb, progress, "273"); // 273: ZAK
			    }
			    else if (filename.endsWith("BfR_berry_supplier.xls")) {
			    	if (transformFormat) ;
			    	else nsf = doImportGaia(wb, progress);
			    }
			    else {
			    	if (transformFormat) {
				    	InputStream isNew = new FileInputStream("C:\\Users\\Armin\\Desktop\\AllKrisen\\NewFormat.xls");
				    	POIFSFileSystem fsNew = new POIFSFileSystem(isNew);
				    	HSSFWorkbook wbNew = new HSSFWorkbook(fsNew);
				    	transformFormat(wb, wbNew);
				    	File f = new File(filename);
				    	File fd = new File(f.getParent() + "/NewFormat");
				    	fd.mkdir();
				    	FileOutputStream out = new FileOutputStream(f.getParent() + "/NewFormat/" + f.getName());
				    	wbNew.write(out);
				    	/*							
						RaspYog nochmal genauer checken
						dutch file when new format established
				    	 */
			    	}
			    	else {
				    	nsf = doImportStandard(wb, progress);
				    	//nsf = doImportNewFormat(wb, progress);
			    	}
			    }
			    int numSuccess = nsf[0];
			    int numFails = nsf[1];

	
			      	DBKernel.myDBi.getTable("Station").doMNs();
			      	DBKernel.myDBi.getTable("Produktkatalog").doMNs();
			      	DBKernel.myDBi.getTable("Chargen").doMNs();
			      	DBKernel.myDBi.getTable("Lieferungen").doMNs();
	    			if (progress != null) {
	  	  			// Refreshen:
	    				MyDBTable myDB = DBKernel.myList.getMyDBTable();
	    				if (myDB.getActualTable() != null) {
		    				String actTablename = myDB.getActualTable().getTablename();
		    				if (actTablename.equals("Produktkatalog") || actTablename.equals("Lieferungen")
		    						 || actTablename.equals("Station") || actTablename.equals("Chargen")) {
		    					myDB.setTable(myDB.getActualTable());
		    				}
	    				}
	    				progress.setVisible(false);
	    			}
	    			if (showResults) {
	    				String log = numSuccess + " erfolgreiche Importe.\n";
	    				log += numFails + " fehlgeschlagene Importe.\n";
	    				InfoBox ib = new InfoBox(log, true, new Dimension(300, 150), null);
	    				ib.setVisible(true);    				
	    			}
			    }
			    catch (Exception e) {MyLogger.handleException(e);}
	      }
	    };
	    Thread thread = new Thread(runnable);
	    thread.start();
	    try {
				thread.join();
			}
	    catch (InterruptedException e) {
	    	MyLogger.handleException(e);
			}
	  }
	private boolean is1SurelyNewer(int deliveryId1, int deliveryId2) {
		Object day1 = DBKernel.getValue("Lieferungen", "ID", deliveryId1+"", "dd_day");
		Object month1 = DBKernel.getValue("Lieferungen", "ID", deliveryId1+"", "dd_month");
		Object year1 = DBKernel.getValue("Lieferungen", "ID", deliveryId1+"", "dd_year");
		Object day2 = DBKernel.getValue("Lieferungen", "ID", deliveryId2+"", "dd_day");
		Object month2 = DBKernel.getValue("Lieferungen", "ID", deliveryId2+"", "dd_month");
		Object year2 = DBKernel.getValue("Lieferungen", "ID", deliveryId2+"", "dd_year");
		return is1SurelyNewer(day1, month1, year1, day2, month2, year2);
	}
	private boolean is1SurelyNewer(Object day1, Object month1, Object year1, Object day2, Object month2, Object year2) {
		if (year1 == null || year2 == null) return false;
		if (Integer.parseInt(year1.toString()) > Integer.parseInt(year2.toString())) return true;
		if (month1 == null || month2 == null || Integer.parseInt(year1.toString()) < Integer.parseInt(year2.toString())) return false;
		if (Integer.parseInt(month1.toString()) > Integer.parseInt(month2.toString())) return true;
		if (day1 == null || day2 == null || Integer.parseInt(month1.toString()) < Integer.parseInt(month2.toString())) return false;
		if (Integer.parseInt(day1.toString()) > Integer.parseInt(day2.toString())) return true;
	    return false;
	}
	private String sdfFormat(String day, String month, String year) {
		return day + "." + month + "." + year;
	}

	private HSSFRow getRow(HSSFSheet sheet, String value, int column) {
		HSSFRow result = null;
		if (value != null && !value.trim().isEmpty()) {
			int numRows = sheet.getLastRowNum() + 1;
	      	for (int i=1;i<numRows;i++) {
	      		HSSFRow row = sheet.getRow(i);
				if (row != null) {
			      String val = getStrVal(row.getCell(column));
			      if (val != null && !val.trim().isEmpty()) {
				      if (value.trim().equalsIgnoreCase(val.trim())) {
				    	  result = row;
				    	  break;
				      }
			      }
				}
	      	}
		}
		return result;
	}
	  private String getStrVal(HSSFCell cell) {
		  return getStrVal(cell, 1023);
	  }
	  private String getStrVal(HSSFCell cell, int maxChars) {
		  String result = null;
			if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
			}
			else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
				result = cell.getStringCellValue();
				if (result.equals(".")) result = null;
			}
			else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC || cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
				try {
					double dbl = cell.getNumericCellValue();
					if (Math.round(dbl) == dbl) result = "" + ((int) dbl);
					else result = "" + cell.getNumericCellValue();
				}
				catch (Exception e) {
					result = cell.getStringCellValue();
				}
			}
			else {
				result = cell.toString();
			}
			if (result != null) {
				if (result.equals("#N/A")) {
					result = null;
				}
				else if (result.length() > maxChars) {
					System.err.println("string too long (" + result.length() + ") - shortened to " + maxChars + " chars... '" + result + "' -> '" + result.substring(0, maxChars) + "'");
					result = result.substring(0, maxChars);
				}
			}
	  	return result;
	  }
	private Integer[] getCharge_Lieferung(String name, String street, String streetNumber, String zip, String city, String county, String country, String kind, String vat,
			String article, String articleNumber, String prodTreatment, String charge, String dayMHD, String monthMHD, String yearMHD, String dayP, String monthP, String yearP, String originCountry, String dayD, String monthD, String yearD, String amountKG, String typePU, String numPU,
			String nameTo, String streetTo, String streetNumberTo, String zipTo, String cityTo, String countyTo, String countryTo, String kindTo, String vatTo,
			String serial, String cqr, 
			String EndChain, String Explanation_EndChain, String Further_Traceback, String MicrobiologicalSample) {
		if (name == null) return null;
		Integer[] result = new Integer[5];

		Integer lastID = null;
		if (name == null) {
		}
		else {
			lastID = getID("Station",
					new String[]{"Name","Strasse","Hausnummer","PLZ","Ort","Bundesland","Land","Betriebsart","VATnumber","CasePriority","Serial"},
					new String[]{name, street, streetNumber, zip, city, county, country, kind, vat, null, serial},
					new boolean[]{true,true,true,true,true,true,true,false,true,false,false},
					new boolean[]{true,true,true,true,true,true,true,true,true,false,true});
			result[0] = lastID;
		}
			if (lastID != null) {
					lastID = getID("Produktkatalog",
							new String[]{"Station","Artikelnummer","Bezeichnung","Prozessierung","Serial"},
							new String[]{lastID.toString(), articleNumber, article, prodTreatment, serial},
							new boolean[]{true,true,true,true,false}, // charge == null || charge.trim().isEmpty()
							new boolean[]{false,true,true,true,true});
					result[1] = lastID;
					if (lastID != null) {
						lastID = getID("Chargen",
								new String[]{"Artikel","ChargenNr","MHD_day","MHD_month","MHD_year","pd_day","pd_month","pd_year","OriginCountry","Serial","MicrobioSample"},
								new String[]{lastID.toString(), charge, dayMHD, monthMHD, yearMHD, dayP, monthP, yearP, originCountry,serial,MicrobiologicalSample},
								new boolean[]{true,true,true,true,true,true,true,true,false,false,false},
								new boolean[]{false,true,false,false,false,false,false,false,true,true,true});
						result[2] = lastID;
						if (lastID != null) {
							Integer empf = null;
							if (nameTo != null && !nameTo.trim().isEmpty()) {
								boolean isCase = nameTo != null && (nameTo.indexOf("Case") >= 0 || nameTo.indexOf("Conf Lot ") >= 0);
								boolean isLot = isCase && nameTo.indexOf("Conf Lot ") >= 0;
								empf = getID("Station",
										new String[]{"Name","Strasse","Hausnummer","PLZ","Ort","Bundesland","Land","Betriebsart","VATnumber","CasePriority","Serial"},
										//new String[]{nameTo, isCase ? street : streetTo, isCase ? streetNumber : streetNumberTo, isCase ? zip : zipTo, isCase ? city : cityTo, isCase ? county : countyTo, isCase ? country : countryTo, kindTo, vatTo, isCase ? (isLot ? "1" : "0.1") : null, serial},
										new String[]{nameTo, streetTo, streetNumberTo, zipTo, cityTo, countyTo, countryTo, kindTo, vatTo, isCase ? (isLot ? "1" : "0.1") : null, serial},
										new boolean[]{true,true,true,true,true,true,true,false,true,false,false},
										new boolean[]{true,true,true,true,true,true,true,true,true,false,true});
								result[4] = empf;
							}
							if (charge == null || charge.trim().isEmpty()) charge = articleNumber + "; " + sdfFormat(dayMHD, monthMHD, yearMHD);;
							//System.err.println(deliveryS);
							lastID = getID("Lieferungen",
									new String[]{"Charge","dd_day","dd_month","dd_year","Unitmenge","UnitEinheit","Empf�nger","Serial","Contact_Questions_Remarks","numPU","typePU","EndChain","Explanation_EndChain","Further_Traceback"},
									new String[]{lastID.toString(), dayD, monthD, yearD, amountKG, "kg", empf == null ? null : empf.toString(),serial, cqr, numPU, typePU, EndChain, Explanation_EndChain, Further_Traceback},
									new boolean[]{true,true,true,true,true,true,true,false,false,true,true,false,false,false},
									new boolean[]{false,false,false,false,false,true,false,true,true,false,true,true,true,true});
							result[3] = lastID;
						}
					}
			}
		return result;
	}
	private Integer getID(String tablename, String[] feldnames, String[] feldVals, boolean[] key, boolean[] isStringType) {
	  Integer result = null;
	  String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(tablename) + " WHERE TRUE";
	  String fns = "";
	  String fvs = "";
	  for (int i=0;i<feldnames.length;i++) {
		  if (feldVals[i] != null && feldVals[i].trim().isEmpty()) feldVals[i] = null;
		  	if (feldVals[i] != null) feldVals[i] = feldVals[i].replaceAll("'", "\\apos");
		  	fns += "," + DBKernel.delimitL(feldnames[i]);
		  	if (feldnames[i].equals("Unitmenge") && feldVals[i] != null) fvs += "," + feldVals[i].replace(",", ".");
		  	else fvs += feldVals[i] != null ? ",'" + feldVals[i] + "'" : ",NULL";
			if (key == null || key[i]) {
				if (isStringType != null && isStringType[i]) sql += " AND " + (feldVals[i] != null ? "UCASE(" + DBKernel.delimitL(feldnames[i]) + ")='" + feldVals[i].toUpperCase() + "'" : DBKernel.delimitL(feldnames[i]) + " IS NULL");
				else sql += " AND " + (feldVals[i] != null ? DBKernel.delimitL(feldnames[i]) + "=" + feldVals[i].replace(",", ".") : DBKernel.delimitL(feldnames[i]) + " IS NULL");	  

			}
	  }
	  /*
	  if (feldVals[0] != null && feldVals[0].equals("")) {
		  System.err.println(sql);
	  }
	   */
	  if (!fns.isEmpty() && !fvs.isEmpty()) {
		  ResultSet rs = DBKernel.getResultSet(sql, false);		  
		  try {
				if (rs != null && rs.first()) {//rs.last() && rs.getRow() == 1) {
					result = rs.getInt(1);
					if (key != null && result != null) {
						boolean doExec = false;
						sql = "UPDATE " + DBKernel.delimitL(tablename) + " SET ";
						int i=0;
						for (boolean b : key) {
							if (!b) {
								if (feldVals[i] != null && !feldVals[i].equalsIgnoreCase("null")) {
									if (sql.endsWith(")")) sql += ",";
									if (isStringType != null && isStringType[i]) sql += DBKernel.delimitL(feldnames[i]) + "=CASEWHEN(" + DBKernel.delimitL(feldnames[i]) + "='" + feldVals[i] + "' OR INSTR(" + DBKernel.delimitL(feldnames[i]) + ",'\n" + feldVals[i] + "')>0," + DBKernel.delimitL(feldnames[i]) + ",IFNULL(CONCAT(" + DBKernel.delimitL(feldnames[i]) + ",'\n','" + feldVals[i] + "'),'" + feldVals[i] + "'))";
									else sql += DBKernel.delimitL(feldnames[i]) + "=('" + feldVals[i] + "')";
									doExec = true;
								}
							}
							i++;
						}
						if (doExec) {
							sql += " WHERE " + DBKernel.delimitL("ID") + "=" + result;
							//System.err.println(sql);
							PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql);
							ps.executeUpdate();
						}
					}
				}

				if (result == null) {
					//System.out.println("result is null\t" + sql);
					boolean alreadyInserted = false;
					if (tablename.equals("Station") && nodeIDs != null) {
						String stationKey = (feldVals[0] == null ? "" : feldVals[0]) + ";;;" +  //"Name"
											(feldVals[1] == null ? "" : feldVals[1]) + ";;;" +  //"Strasse"
											(feldVals[2] == null ? "" : feldVals[2]) + ";;;" +  //"Hausnummer"
											(feldVals[3] == null ? "" : feldVals[3]) + ";;;" +  //"PLZ"
											(feldVals[4] == null ? "" : feldVals[4]) + ";;;" +  //"Ort"
											(feldVals[5] == null ? "" : feldVals[5]) + ";;;" +  //"Bundesland"
											(feldVals[6] == null ? "" : feldVals[6]) + ";;;" +  //"Land"
											(feldVals[8] == null ? "" : feldVals[8]) + ";;;"; //"VAT"
						if (nodeIDs.containsKey(stationKey)) {
							Integer id = nodeIDs.get(stationKey);
							sql = "INSERT INTO " + DBKernel.delimitL(tablename) + " (ID," + fns.substring(1) +	") VALUES (" + id + "," + fvs.substring(1) + ")";						
							PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql);					
							if (ps.executeUpdate() > 0) {
								result = id;
								alreadyInserted = true;
							}
						}
						else {
							System.err.println("Station id not found???? -> " + stationKey);
						}
					}
					if (!alreadyInserted) {
						if (tablename.equals("Station") && nodeIDs != null) {
							maxNodeID++;
							sql = "INSERT INTO " + DBKernel.delimitL(tablename) + " (ID, " + fns.substring(1) + ") VALUES (" + maxNodeID + "," + fvs.substring(1) + ")";						
							PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql);					
							if (ps.executeUpdate() > 0) result = maxNodeID;
						}
						else {
							sql = "INSERT INTO " + DBKernel.delimitL(tablename) + " (" + fns.substring(1) +	") VALUES (" + fvs.substring(1) + ")";						
							PreparedStatement ps = DBKernel.getDBConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);					
							if (ps.executeUpdate() > 0) result = DBKernel.getLastInsertedID(ps);
						}
					}
					
				}
				else {
					//System.out.println(result + "\t" + sql);
				}
			}
			catch (Exception e) {
				if (sql.startsWith("UPDATE") && e.getMessage().startsWith("data exception: string data, right truncation")) ;
				else {
					System.err.println(sql);
					MyLogger.handleException(e);					
				}
			}
	  }
		
		return result;
	}
	class XlsFileFilter extends FileFilter {
	    public boolean accept(File f) {
	        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xls");
	    }
	    
	    public String getDescription() {
	        return ".xls files";
	    }
	}}
