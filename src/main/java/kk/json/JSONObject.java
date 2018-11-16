/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package kk.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A JSON object. Key value pairs are in the order of adding.
 * JSONObject supports java.util.Map interface.
 *
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONObject<K, V> extends HashMap<K, V>
		implements Map<K, V>, JSONAware {
	private static final long serialVersionUID = -503443796854799292L;

	public JSONObject() {
		super();
	}

	public JSONObject(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public JSONObject(int initialCapacity) {
		super(initialCapacity);
	}

	public JSONObject(Map<? extends K, ? extends V> m) {
		super(m);
	}

	public JSONObject(K key, V value) {
		super(1);
		put(key, value);
	}


	/**
	 * Convert (aka., encode) a map to JSON text. The result is a JSON object.
	 * If this map is also a JSONAware, JSONAware specific behaviors will be omitted at this top level.
	 *
	 * @see JSONValue#toJSONString(Object)
	 *
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toJSONString(Map map){
		if(map == null)
			return "null";

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		Iterator iter=map.entrySet().iterator();

		sb.append('{');
		while(iter.hasNext()){
			if(first)
				first = false;
			else
				sb.append(',');

			Map.Entry entry=(Map.Entry)iter.next();
			toJSONString(String.valueOf(entry.getKey()),entry.getValue(), sb);
		}
		sb.append('}');
		return sb.toString();
	}

	/** Encodes this object to a JSON string.
	 * It is the same as {@link #toString()}.
	 */
	public String toJSONString(){
		return toJSONString(this);
	}

	private static String toJSONString(String key,Object value, StringBuilder sb){
		sb.append('\"');
		if(key == null)
			sb.append("null");
		else
			JSONValue.escape(key, sb);
		sb.append('\"').append(':');

		sb.append(JSONValue.toJSONString(value));

		return sb.toString();
	}

	/** Encodes this object to a JSON string.
	 * It is the same as {@link #toJSONString()}.
	 */
	public String toString(){
		return toJSONString();
	}

	public static String toString(String key,Object value){
		StringBuilder sb = new StringBuilder();
		toJSONString(key, value, sb);
		return sb.toString();
	}
}
