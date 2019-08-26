package scraping;

import java.io.File;
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

		String file = "ejemplo.html";

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// elemento raiz
		org.w3c.dom.Document doc = docBuilder.newDocument();
		org.w3c.dom.Element rootElement = doc.createElement("game");
		doc.appendChild(rootElement);

//		if (getStatusConnectionCode(url) == 200) {
		if (getStatusFile(file) == 1) {

//			Document documento = getHtmlDocument(url);
			Document documento = getHtmlFileToDocument(file);

//			Analizando el grupo de bateadores del team VS
			Elements elementosOffensiveVs = documento
					.select("table[id=MainContent_Estado_Juego_Tabs_ctl44_BoxScore_Bateo_VS_DXMainTable] > tbody > tr");
			System.out.println(elementosOffensiveVs.size());
			rootElement.appendChild(extractOffensiveHtmlToXml( elementosOffensiveVs, doc));
			
//			Analizando el grupo de bateadores del team HC
			Elements elementosOffensiveHc = documento
					.select("table[id=MainContent_Estado_Juego_Tabs_ctl44_BoxScore_Bateo_HC_DXMainTable] > tbody > tr");
			System.out.println(elementosOffensiveHc.size());
			rootElement.appendChild(extractOffensiveHtmlToXml( elementosOffensiveHc, doc));
			
//			Analizando el grupo de bateadores del team VS
			Elements elementosPitchVs = documento
					.select("table[id=MainContent_Estado_Juego_Tabs_ctl44_BoxScore_Pitch_VS_DXMainTable] > tbody > tr");
			System.out.println(elementosPitchVs.size());
			rootElement.appendChild(extractPitchHtmlToXml( elementosPitchVs, doc));
			
//			Analizando el grupo de bateadores del team HC
			Elements elementosPitchHc = documento
					.select("table[id=MainContent_Estado_Juego_Tabs_ctl44_BoxScore_Pitch_HC_DXMainTable] > tbody > tr");
			System.out.println(elementosPitchHc.size());
			rootElement.appendChild(extractPitchHtmlToXml( elementosPitchHc, doc));

			
			
			
			
		}

		// nombre del fichero
		Date fecha = new Date();
		DateFormat hourdateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		System.out.println("Hora y fecha: " + hourdateFormat.format(fecha));
		String nombreFichero = hourdateFormat.format(fecha);

		// escribimos el contenido en un archivo .xml
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		String ruta = "dataXML\\";
		StreamResult result = new StreamResult(new File(ruta, nombreFichero + ".xml"));

		// StreamResult result = new StreamResult(new File("archivo.xml"));
		// Si se quiere mostrar por la consola...
		// StreamResult result = new StreamResult(System.out);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private static org.w3c.dom.Element extractOffensiveHtmlToXml(Elements elementos,org.w3c.dom.Document doc) {
		
		org.w3c.dom.Element players = doc.createElement("players");
		
		for (Element elem : elementos) {

			// para no tomar la primera entrada que tiene el encabezado
			if (!(elem.equals(elementos.first()))) {
//				System.out.println("ok");

				org.w3c.dom.Element player = doc.createElement("player");
				players.appendChild(player);
				Integer contador = 0;
				Elements playerData = elem.select("td");
				for (Element playerElement : playerData) {
					contador++;
					
					//analisis de el id en sn
					Element playerDataId = elem.select("a").get(0);
					if (playerDataId != null) {
						String playerDataIdA = playerDataId.attr("href");
						// atributo del player
						Attr attr = doc.createAttribute("id");
						attr.setValue(extractIdLink(playerDataIdA));
						player.setAttributeNode(attr);
					}
					
					String attrName = "";
					switch(contador) {
					case 1: attrName = "name";
					break;
					case 2: attrName = "vb";
					break;
					case 3: attrName = "c";
					break;
					case 4: attrName = "h";
					break;
					case 5: attrName = "b2";
					break;
					case 6: attrName = "b3";
					break;
					case 7: attrName = "hr";
					break;
					case 8: attrName = "ci";
					break;
					case 9: attrName = "o";
					break;
					case 10: attrName = "a";
					break;
					case 11: attrName = "e";
					break;
					}
					String cadena = playerElement.text();

					// atributo del player
					Attr attr = doc.createAttribute(attrName);
					attr.setValue(cadena);
					player.setAttributeNode(attr);
				}
			}

		}
		return players;
	}

private static String extractIdLink(String cadena) {
	String[] cadenaSplit = cadena.split("id=");
	return cadenaSplit[1];
}	
	
private static org.w3c.dom.Element extractPitchHtmlToXml(Elements elementos,org.w3c.dom.Document doc) {
		
		org.w3c.dom.Element players = doc.createElement("players");
		
		for (Element elem : elementos) {

			// para no tomar la primera entrada que tiene el encabezado
			if (!(elem.equals(elementos.first()))) {
//				System.out.println("ok");

				org.w3c.dom.Element player = doc.createElement("player");
				players.appendChild(player);
				Integer contador = 0;
				Elements playerData = elem.select("td");
				for (Element playerElement : playerData) {
					contador++;
					String attrName = "";
					switch(contador) {
					case 1: attrName = "name";
					break;
					case 2: attrName = "vb";
					break;
					case 3: attrName = "h";
					break;
					case 4: attrName = "c";
					break;
					case 5: attrName = "cl";
					break;
					case 6: attrName = "so";
					break;
					case 7: attrName = "bb";
					break;
					case 8: attrName = "bi";
					break;
					case 9: attrName = "wp";
					break;
					case 10: attrName = "db";
					break;
					case 11: attrName = "bk";
					break;
					case 12: attrName = "inn";
					break;
					}
					String cadena = playerElement.text();

					// atributo del player
					Attr attr = doc.createAttribute(attrName);
					attr.setValue(cadena);
					player.setAttributeNode(attr);
				}
			}

		}
		return players;
	}
}
