package morlivm.memory;

public interface Loadable
{
	public LoadedSectionsQueue upload();
	public void execute();
	public String getLoadedUnitCode();
	public boolean uploadTest();
}
