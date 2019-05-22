package morlivm.control_panel;
import java.awt.Dimension;

public interface Purchasable
{
	public boolean checkPurchaseRelevance();
	public String getDeclineErrorMessage();
	public String getAdDescription();
	public Dimension getAdDimension();
	public void purchase();
}