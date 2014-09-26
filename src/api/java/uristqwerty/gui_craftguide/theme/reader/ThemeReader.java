package uristqwerty.gui_craftguide.theme.reader;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.gui_craftguide.theme.Theme;

public class ThemeReader implements ContentHandler
{
	private static ElementHandler metadataElement = new GenericHandler(
			"id", new MetadataProperty("id"),
			"name", new MetadataProperty("name"),
			"description", new MetadataProperty("description"),
			"dependency", DependencyElement.class);

	private static ElementHandler themeElement = new GenericHandler(
			"metadata", metadataElement,
			"image", ImageElement.class,
			"texture", TextureElement.class);

	private static ElementHandler rootElement = new GenericHandler(
			"theme", themeElement);

	private LinkedList<ElementHandler> handlerStack = new LinkedList<ElementHandler>();

	private Theme theme;

	public Theme read(InputStream source, File themeDir)
	{
		theme = new Theme(themeDir);
		handlerStack.add(rootElement);

		try
		{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(this);
			reader.parse(new InputSource(source));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			CraftGuideLog.log(e);
			return null;
		}

		handlerStack.clear();
		return theme;
	}

	@Override
	public void startDocument() throws SAXException
	{
	}

	@Override
	public void endDocument() throws SAXException
	{
	}

	@Override
	public void characters(char[] chars, int start, int length) throws SAXException
	{
		if(handlerStack.peek() != null)
		{
			handlerStack.peek().characters(theme, chars, start, length);
		}
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes attributes) throws SAXException
	{
		if(handlerStack.peek() == null)
		{
			CraftGuideLog.log("CraftGuide: Error loading theme file. Stack is null at element '" + name + "'", true);
		}
		else
		{
			ElementHandler h = handlerStack.peek().getSubElement(name, attributes);
			handlerStack.push(h != null? h : NullElement.instance);
			handlerStack.peek().startElement(theme, name, attributes);
		}
	}

	@Override
	public void endElement(String uri, String name, String qName) throws SAXException
	{
		ElementHandler handler = handlerStack.pop();
		handler.endElement(theme, name);

		if(handlerStack.peek() != null)
		{
			handlerStack.peek().endSubElement(theme, handler, name);
		}
	}

	@Override
	public void processingInstruction(String arg0, String arg1) throws SAXException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setDocumentLocator(Locator locator)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void skippedEntity(String name) throws SAXException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ignorableWhitespace(char[] chars, int start, int length) throws SAXException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException
	{
		// TODO Auto-generated method stub
	}
}
