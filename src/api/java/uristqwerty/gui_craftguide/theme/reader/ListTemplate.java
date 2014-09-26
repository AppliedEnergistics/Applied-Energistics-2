package uristqwerty.gui_craftguide.theme.reader;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class ListTemplate implements ValueTemplate
{
	private int size = -1;
	private ArrayList list = new ArrayList();
	private Class type;
	public Object result;

	public ListTemplate(Class type)
	{
		this.type = type;
	}

	public ListTemplate(Class type, int size)
	{
		this.type = type;
		this.size = size;
	}

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
	}

	@Override
	public ElementHandler getSubElement(String name, Attributes attributes)
	{
		ValueTemplate t = ValueType.getTemplate(type.getComponentType());

		if(t != null)
		{
			return t;
		}
		else
		{
			return NullElement.instance;
		}
	}

	@Override
	public void endElement(Theme theme, String name)
	{
		if(size == -1)
		{
			size = list.size();
		}

		result = Array.newInstance(type.getComponentType(), size);

		for(int i = 0; i < Math.min(size, list.size()); i++)
		{
			Array.set(result, i, list.get(i));
		}
	}

	@Override
	public void endSubElement(Theme theme, ElementHandler handler, String name)
	{
		if(handler instanceof ValueTemplate)
		{
			list.add(((ValueTemplate)handler).getValue());
		}
	}

	@Override
	public Class valueType()
	{
		return type;
	}

	@Override
	public Object getValue()
	{
		return result;
	}
}
