package org.wuwei.archon.nexon.manage;

public interface Agent extends Component {
	public interface VariableChangeCallback {
		public void variableChanged(String variableGroupName, String variableName, Object value, int ... indices);
		public default Object variableObserved(String variableGroupName, String variableName, int ... indices) {
			return null;
		}
	}
	public NexonData getData();
	public void setCallback(VariableChangeCallback callback);
	public VariableChangeCallback getCallback();
}
