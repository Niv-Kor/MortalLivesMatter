package morlivm.system.graphics.sheet;
import java.awt.Dimension;

public abstract class Sheet
{
	protected String path;
	protected Dimension dim;
	protected Pamphlet mngr;
	
	public String getPath() { return path; }
	public Dimension getDimension() { return dim; }
	public Pamphlet getSheetFolder() { return mngr; }
}