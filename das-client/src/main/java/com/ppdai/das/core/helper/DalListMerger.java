package com.ppdai.das.core.helper;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ComparisonChain;
import com.ppdai.das.client.sqlbuilder.ColumnOrder;
import com.ppdai.das.core.ResultMerger;


public class DalListMerger<T> implements ResultMerger<List<T>> {
	private List<T> result = new ArrayList<>();
	private Comparator<T> comparator;

	public DalListMerger() {
		this(null);
	}
	
	public DalListMerger(List<ColumnOrder> sorters) {
		this.comparator = (o1, o2) ->{
			ComparisonChain comparisonChain = ComparisonChain.start();
			for(ColumnOrder sorter: sorters){
				try {
					Field f = o1.getClass().getDeclaredField(Introspector.decapitalize(sorter.getColumn().getColumn().getColumnName()));
					f.setAccessible(true);
					Object v1 = f.get(o1);
					Object v2 = f.get(o2);
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
		if(partial!=null)
			result.addAll(partial);
	}

	@Override
	public List<T> merge() {
		if(comparator != null)
			Collections.sort(result, comparator);
		return result;
	}
}