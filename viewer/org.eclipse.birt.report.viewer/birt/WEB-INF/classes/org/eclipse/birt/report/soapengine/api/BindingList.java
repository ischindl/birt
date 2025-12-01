package org.eclipse.birt.report.soapengine.api;

import java.util.Arrays;

public class BindingList {
	private Binding[] binding;

	public BindingList() {
	}

	public BindingList(Binding[] binding) {
		this.binding = binding;
	}

	public Binding[] getBinding() {
		return binding;
	}

	public void setBinding(Binding[] binding) {
		this.binding = binding;
	}

	public Binding getBinding(int i) {
		return binding[i];
	}

	public void setBinding(int i, Binding value) {
		binding[i] = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BindingList))
			return false;
		if (this == obj)
			return true;
		BindingList other = (BindingList) obj;
		return Arrays.equals(this.binding, other.binding);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(binding);
	}
}
