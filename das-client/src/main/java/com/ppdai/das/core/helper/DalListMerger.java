package com.ppdai.das.core.helper;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ComparisonChain;
import com.google.gson.Gson;
import com.ppdai.das.client.sqlbuilder.ColumnOrder;
import com.ppdai.das.core.ResultMerger;
import com.ppdai.das.service.Entity;


public class DalListMerger<T> implements ResultMerger<List<T>> {
	private List<T> result = new ArrayList<>();
	private Comparator<T> comparator;

	public DalListMerger() {
		this(null);
	}
	
	public DalListMerger(List<ColumnOrder> sorters) {
		if(sorters == null || sorters.isEmpty()) {
			return;
		}

		this.comparator = (o1, o2) ->{
			final Comparable least = (i) -> Integer.MIN_VALUE;
			ComparisonChain comparisonChain = ComparisonChain.start();
			for(ColumnOrder sorter: sorters){
				try {
					Object v1;
					Object v2;
					String colName = sorter.getColumn().getColumn().getColumnName();
					if (o1 instanceof Map || o2 instanceof Map) {//Map entity
						v1 = ((Map)o1).get(colName);
						v2 = ((Map)o2).get(colName);

					} else if(o1 instanceof Entity || o2 instanceof Entity){
						v1 = new Gson().fromJson(((Entity)o1).getValue(), Map.class).get(colName);
						v2 = new Gson().fromJson(((Entity)o2).getValue(), Map.class).get(colName);
					} else {//POJO entity
						Field f = o1.getClass().getDeclaredField(Introspector.decapitalize(colName));
						f.setAccessible(true);
						v1 = f.get(o1);
						v2 = f.get(o2);
					}
					v1 = Optional.of(v1).orElse(least);
					v2 = Optional.of(v2).orElse(least);
					if(sorter.isAsc()) {
						comparisonChain = comparisonChain.compare((Comparable)v1, (Comparable)v2);
					} else {
						comparisonChain = comparisonChain.compare((Comparable)v2, (Comparable)v1);
					}
				}catch (NoSuchFieldException |IllegalAccessException e){
					throw new RuntimeException(e);
				}
			}
			return comparisonChain.result();
		};
	}

	@Override
	public void addPartial(String shard, List<T> partial) {
		if(partial!=null && !partial.isEmpty()) {
            result.addAll(partial);
        }
	}

	@Override
	public List<T> merge() {
		if(comparator != null) {
			result.sort(comparator);
		}

		return result;
	}
}