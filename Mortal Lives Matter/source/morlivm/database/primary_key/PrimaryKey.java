package morlivm.database.primary_key;

public abstract class PrimaryKey
{
	protected Object[] keys;
	protected int size, i;
	protected String classID;
	
	public PrimaryKey(int size) {
		this.size = size;
		this.keys = new Object[size];
		this.i = 0;
		this.classID = "";
	}
	
	protected void add(Object keyComponent) {
		keys[i++] = keyComponent;
	}
	
	public String getKey() {
		String str = "" + classID + ": ";
		String keyRepresentation;
		
		for (int i = 0; i < size; i++) {
			keyRepresentation = stringConversion(keys[i]);
			str = str.concat(keyRepresentation);
			if (i < size - 1) str = str.concat(" | ");
		}

		return str;
	}
	
	private String stringConversion(Object obj) {
		Class<?> c = obj.getClass();
		
		if (c == String.class) return new String((String) obj);
		if (c == Integer.class) return "" + ((Integer) obj).intValue();
		if (c == Double.class) return "" + ((Double) obj).doubleValue();
		if (c == Boolean.class) return (((Boolean) obj).booleanValue() == true) ? "true" : "false";
		
		System.err.println("Could not convert a primary key of type " + c.getName());
		return null;
	}
	
	public Object getKeyComponent(int i) { return keys[i]; }
}