import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class Environment {
    private final HashMap<String,Double> variableValues = new HashMap<>();
    private final Map<String, Double> monitors = new HashMap<>();
    private boolean traceMode;

    public Environment() { }

    public void addMonitorIfAbsent(String key, Double result) {
    	monitors.computeIfAbsent(key, (k) -> result);
		}

		private void updateMonitor(String key, Double value) {
    	monitors.computeIfPresent(key, (k, v) -> value);
		}

		public Map<String, Double> getMonitors() {
    	return monitors;
		}

    public void flipTraceMode() {
    	traceMode = !traceMode;
		}

		public boolean getTraceMode() {
    	return traceMode;
		}

    public void setVariable(String name, Double value) {
    	updateMonitor(name, value);
			variableValues.put(name, value);
    }
    
    public Double getVariable(String name) {
			Double value = variableValues.get(name);

			if (value == null) {
				System.err.println("Variable not defined: "+name);
				System.exit(-1);
			}

			return value;
    }

	public HashMap<String, Double> getVariableValues() {
		return variableValues;
	}

	public void checkVariable(String name, Double value){
			Double v = variableValues.get(name);
			if (v == null) {
				variableValues.put(name, value);
			} else if (!v.equals(value)) {
				System.err.println("Variable already defined with different type: "+name);
				System.exit(-1);
			}
    }
    
    public String toString() {
			StringBuilder table = new StringBuilder();

			for (Entry<String,Double> entry : variableValues.entrySet()) {
				table.append(entry.getKey()).append("\t-> ").append(entry.getValue()).append("\n");
			}

			return table.toString();
    }   
}

