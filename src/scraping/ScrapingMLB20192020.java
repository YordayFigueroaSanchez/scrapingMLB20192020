package scraping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Attr;

public class ScrapingMLB20192020 {

	public static final String xmlFilePath = "xmlfile.xml";

	public static void main(String[] args) throws ParserConfigurationException, TransformerConfigurationException {
		// TODO Auto-generated method stub

		String url = "https://coinmarketcap.com/";

		String file = "ejemploMLB.html";

		File input = new File("data/" + file);
		Document doc = null;
		try {
			doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// elemento raiz
//		Document doc = docBuilder.newDocument();
		Element gameElement = doc.createElement("game");
		doc.appendChild(gameElement);

//		if (getStatusConnectionCode(url) == 200) {
		if (getStatusFile(file) == 1) {

//			Document documento = getHtmlDocument(url);
			Document documento = getHtmlFileToDocument(file);

//			Analizando el score del juego
			Elements elementosScore = documento.select("table.mlb-scores > tbody");
			System.out.println(elementosScore.size());
//			rootElement.appendChild(extractPitchHtmlToXml( elementosPitchHc, doc));

//			Analizando el grupo de bateadores de ambos teams
			Elements elementosOffensive = documento.select("table.mlb-box-bat > tbody");
			System.out.println(elementosOffensive.size());
			gameElement.appendChild(extractOffensiveHtmlToXml(elementosOffensive.get(0).select("tr"), doc));
			gameElement.appendChild(extractOffensiveHtmlToXml(elementosOffensive.get(1).select("tr"), doc));

//			Analizando el grupo de bateadores del team HC
//			Elements elementosOffensiveHc = documento
//					.select("table[id=MainContent_Estado_Juego_Tabs_ctl44_BoxScore_Bateo_HC_DXMainTable] > tbody > tr");
//			System.out.println(elementosOffensiveHc.size());
//			rootElement.appendChild(extractOffensiveHtmlToXml( elementosOffensiveHc, doc));

//			Analizando el grupo de lanzadores de ambos team
			Elements elementosPitch = documento.select("table.mlb-pitch > tbody");
			System.out.println(elementosPitch.size());
			gameElement.appendChild(extractPitchHtmlToXml(elementosPitch.get(0).select("tr"), doc));
			gameElement.appendChild(extractPitchHtmlToXml(elementosPitch.get(1).select("tr"), doc));

		}

		// nombre del fichero
		Date fecha = new Date();
		DateFormat hourdateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		System.out.println("Hora y fecha: " + hourdateFormat.format(fecha));
		String nombreFichero = hourdateFormat.format(fecha);

		// escribimos el contenido en un archivo .xml
		String ruta = "dataXML\\";

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(ruta + nombreFichero + ".xml"));
			System.out.println(gameElement.outerHtml());
			writer.write(gameElement.outerHtml());

		} catch (IOException e) {
			System.out.println("error");
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("File saved!");

	}

	/**
	 * Con esta método compruebo el Status code de la respuesta que recibo al hacer
	 * la petición EJM: 200 OK 300 Multiple Choices 301 Moved Permanently 305 Use
	 * Proxy 400 Bad Request 403 Forbidden 404 Not Found 500 Internal Server Error
	 * 502 Bad Gateway 503 Service Unavailable
	 * 
	 * @param url
	 * @return Status Code
	 */
	public static int getStatusConnectionCode(String url) {

		Response response = null;

		try {
			response = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).ignoreHttpErrors(true).execute();
		} catch (IOException ex) {
			System.out.println("Excepción al obtener el Status Code: " + ex.getMessage());
		}
		return response.statusCode();
	}

	/**
	 * Con este método devuelvo un objeto de la clase Document con el contenido del
	 * HTML de la web que me permitirá parsearlo con los métodos de la librelia
	 * JSoup
	 * 
	 * @param url
	 * @return Documento con el HTML
	 */
	public static Document getHtmlDocument(String url) {

		Document doc = null;
		try {
			doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).get();
		} catch (IOException ex) {
			System.out.println("Excepción al obtener el HTML de la página" + ex.getMessage());
		}
		return doc;
	}

	public static int getStatusFile(String file) {
		return 1;
	}

	public static Document getHtmlFileToDocument(String file) {

		File input = new File("data/" + file);
		Document doc = null;
		try {
			doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		} catch (IOException ex) {
			System.out.println("Excepción al obtener el HTML de la página" + ex.getMessage());
		}
		return doc;
	}

	private static Element extractOffensiveHtmlToXml(Elements elementos, Document doc) {

		Element players = doc.createElement("batters");

		for (Element elem : elementos) {

			// para no tomar la primera entrada que tiene el encabezado
//			if (!(elem.equals(elementos.first()))) {
//				System.out.println("ok");

			Element player = doc.createElement("player");
			players.appendChild(player);
			Integer contador = 0;
			Elements playerData = elem.select("td");
			for (Element playerElement : playerData) {
				contador++;

				// tomar el data-label
				// String dataLabel = playerElement.attr("data-label");

				String attrName = "";
				String cadena = "";

				switch (contador) {
				case 1:
					attrName = "name";
					cadena = playerElement.text();
					cadena = extractElementBefore(cadena);
					//cadena = cadena2.trim();
					Elements playerDataIds = elem.select("a");

					if (!playerDataIds.isEmpty()) {
						Element playerDataId = playerDataIds.get(0);
						String playerDataIdA = playerDataId.attr("href");
						// atributo del player
						player.attr("id", extractIdLink(playerDataIdA));
					}
					break;
				case 2:
					attrName = "ab";
					cadena = playerElement.text();
					break;
				case 3:
					attrName = "r";
					cadena = playerElement.text();
					break;
				case 4:
					attrName = "h";
					cadena = playerElement.text();
					break;
				case 5:
					attrName = "rbi";
					cadena = playerElement.text();
					break;
				case 6:
					attrName = "bb";
					cadena = playerElement.text();
					break;
				case 7:
					attrName = "sb";
					cadena = playerElement.text();
					break;
				case 8:
					attrName = "so";
					cadena = playerElement.text();
					break;
				case 9:
					attrName = "lob";
					cadena = playerElement.text();
					break;
				case 10:
					attrName = "ave";
					cadena = playerElement.text();
					break;
				case 11:
					attrName = "obp";
					cadena = playerElement.text();
					break;
				case 12:
					attrName = "slg";
					cadena = playerElement.text();
					break;
				case 13:
					attrName = "ops";
					cadena = playerElement.text();
					break;
				}
				// String cadena = playerElement.text();

				// atributo del player
//					Attr attr = doc.createAttribute(attrName);
//					attr.setValue(cadena);
//					player.setAttributeNode(attr);
				player.attr(attrName, cadena.trim());

			}
//			}

		}
		return players;
	}

	private static String extractIdLink(String cadena) {
		String[] cadenaSplit = cadena.split("player/");
		return cadenaSplit[1];
	}
	
	private static String extractElementBefore(String cadena) {
		while(!Character.isLetter(cadena.charAt(0))) {
			cadena = cadena.substring(1,cadena.length());
		}
		//String[] cadenaSplit = cadena.split(";");
		//return cadenaSplit[cadenaSplit.length-1];
		return cadena;
	}

	private static Element extractPitchHtmlToXml(Elements elementos, Document doc) {

		Element players = doc.createElement("pitchers");

		for (Element elem : elementos) {

			// para no tomar la primera entrada que tiene el encabezado
//			if (!(elem.equals(elementos.first()))) {
//				System.out.println("ok");

			Element player = doc.createElement("player");
			players.appendChild(player);
			Integer contador = 0;
			String cadena	= "";
			Elements playerData = elem.select("td");
			for (Element playerElement : playerData) {
				contador++;
				String attrName = "";
				switch (contador) {
				case 1:
					attrName = "name";
					cadena = playerElement.text();
					cadena = extractElementBefore(cadena);
					//cadena = cadena2.trim();
					Elements playerDataIds = elem.select("a");

					if (!playerDataIds.isEmpty()) {
						Element playerDataId = playerDataIds.get(0);
						String playerDataIdA = playerDataId.attr("href");
						// atributo del player
						player.attr("id", extractIdLink(playerDataIdA));
					}
					break;
				case 2:
					attrName = "ip";
					cadena = playerElement.text();
					break;
				case 3:
					attrName = "h";
					cadena = playerElement.text();
					break;
				case 4:
					attrName = "r";
					cadena = playerElement.text();
					break;
				case 5:
					attrName = "er";
					cadena = playerElement.text();
					break;
				case 6:
					attrName = "bb";
					cadena = playerElement.text();
					break;
				case 7:
					attrName = "k";
					cadena = playerElement.text();
					break;
				case 8:
					attrName = "hr";
					cadena = playerElement.text();
					break;
				case 9:
					attrName = "pc-st";
					cadena = playerElement.text();
					break;
				case 10:
					attrName = "era";
					cadena = playerElement.text();
					break;
//					case 11: attrName = "bk";
//					break;
//					case 12: attrName = "inn";
//					break;
				}
//				String cadena = playerElement.text();

				// atributo del player
				player.attr(attrName, cadena.trim());
			}
//			}

		}
		return players;
	}
}
