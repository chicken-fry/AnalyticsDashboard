package com.emirates.dash.utilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapSortUtil {

//	//sorting method 1
//	public static <K, V extends Comparable<? super V>> Map<K, V> 
//    sortByValue(Map<K, V> map) {
//    List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
//    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
//        @Override
//        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
//            return (o1.getValue()).compareTo(o2.getValue());
//        }
//    });
//
//    Map<K, V> result = new LinkedHashMap<>();
//    for (Map.Entry<K, V> entry : list) {
//        result.put(entry.getKey(), entry.getValue());
//    }
//    return result;
//    }
	
	//sorting method 2 : in descending order
    public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValueDescending(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return (o1.getValue()).compareTo( o2.getValue() );
        }
    });
    Collections.reverse(list);

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
        result.put(entry.getKey(), entry.getValue());
    }
    return result;
    }
    
	//sorting method 2 : in ascending order
    public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValue(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return (o1.getValue()).compareTo( o2.getValue() );
        }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
        result.put(entry.getKey(), entry.getValue());
    }
    return result;
    }
    
    
    
  //sorting method 2 : in descending order
    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>>
    sortByValueDescendingAndReturnList(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return (o1.getValue()).compareTo( o2.getValue() );
        }
    });
    Collections.reverse(list);
    return list;
//    Map<K, V> result = new LinkedHashMap<K, V>();
//    for (Map.Entry<K, V> entry : list) {
//        result.put(entry.getKey(), entry.getValue());
//    }
//    return result;
    }
    
	//sorting method 2 : in ascending order
    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>>
    sortByValueAndReturnList(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return (o1.getValue()).compareTo( o2.getValue() );
        }
    });
    return list;
    
//    Map<K, V> result = new LinkedHashMap<K, V>();
//    for (Map.Entry<K, V> entry : list) {
//        result.put(entry.getKey(), entry.getValue());
//    }
//    return result;
    }
    
}
