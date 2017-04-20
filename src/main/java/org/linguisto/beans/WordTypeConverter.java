package org.linguisto.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.linguisto.db.DBManager;
import org.linguisto.db.obj.WordType;

public class WordTypeConverter implements Converter {

	private Map<Integer,WordType> typeMap;
	private List<WordType> types;

	public WordTypeConverter() {
	}

	protected void init(Locale lang, DBManager dbManager) {
		typeMap = new HashMap<Integer, WordType>();
		types = dbManager.getAllWordTypes(lang);
		for (WordType type : types) {
			typeMap.put(type.getId(), type);
		}
	}
	
	public List<WordType> getTypes() {
		return types;
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object obj) {
		String ret = "---";
		if (obj != null && obj instanceof Number) {
			Number num = (Number)obj;
			WordType wt = typeMap.get(num.intValue());
			if (wt != null) {
				ret = wt.getDesc();
			}
		}
		return ret;
	}

}
