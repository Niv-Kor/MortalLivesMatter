package morlivm.control_panel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import morlivm.main.Game;
import morlivm.system.UI.Button;
import morlivm.system.UI.Cursor;
import morlivm.system.UI.DraggingDevice;
import morlivm.system.UI.Point;
import morlivm.system.graphics.Collider;
import morlivm.system.graphics.ExtendedGraphics2D;
import morlivm.system.graphics.Graphable;
import morlivm.system.graphics.ImageHandler;
import morlivm.system.math.Physics;

public class Notifier
{
	private static class Notification implements Graphable
	{
		private final static Font FONT = new Font("Aller Display", Font.BOLD, 23);
		private final static Dimension DIM = new Dimension(350, 332);
		private final static Dimension BUTTON_DIM = new Dimension(122, 47);
		private final static String PATH = "/Sprites/UI Components/Notice/";
		private final static int MAX_TITLE = 9;
		
		private Point point;
		private String title, rawMsg;
		private BufferedImage sign;
		private Button accept, decline;
		private Collider collider;
		private DraggingDevice draggingDevice;
		private boolean hover;
		private List<String> displayMsgs;
		
		public Notification(String title, String msg, boolean declineButton) {
			this.point = new Point(Game.WIDTH / 2 - DIM.width / 2, Game.HEIGHT / 2 - DIM.height / 2 - 50);
			this.draggingDevice = new DraggingDevice(this, DraggingDevice.Route.FREE);
			this.collider = new Collider(point, DIM);
			this.sign = ImageHandler.load(PATH + "Sign.png");
			this.title = new String(title.toUpperCase());
			if (title.length() > MAX_TITLE) {
				System.err.println("The notice title " + this.title + " has been shortened "
								 + "to " + MAX_TITLE + " characters");
				
				this.title = title.substring(0, MAX_TITLE + 1);
			}
			
			this.rawMsg = new String(msg.toUpperCase());
			fixMessage();
			displayMsgs = Arrays.asList(rawMsg.split("(?<=[@])"));
			for (int i = 0; i < displayMsgs.size(); i++)
				displayMsgs.set(i, displayMsgs.get(i).replace('@', ' '));
			
			Point acceptP;
			if (declineButton) {
				acceptP = new Point(point, 45, DIM.height - BUTTON_DIM.height - 30, 0);
				decline = new Button(PATH + "Decline.png", PATH + "DeclineFcs.png",
									 null, new Point(point, 60 + BUTTON_DIM.width,
									 DIM.height - BUTTON_DIM.height - 30, 0), BUTTON_DIM);
			}
			else acceptP = new Point(point, DIM.width / 2 - BUTTON_DIM.width / 2,
									 DIM.height - BUTTON_DIM.height - 30, 0);
			
			this.accept = new Button(PATH + "OK.png", PATH + "OKFcs.png", null, acceptP, BUTTON_DIM);
		}

		public void update(double delta) {
			hover = Game.getMouseInput().hover("hover " + this, collider, Cursor.POINTER, Cursor.CLICKER);
			draggingDevice.update(delta);
		}
		
		public void render(ExtendedGraphics2D g) {
			g.drawImage(sign, point, DIM);
			g.setFont(FONT);
			
			Point titleP = new Point(point, 110, 42, 0);
			Point msgP = new Point(point, 40, 120, 0);
			
			g.drawOutlineString(title, titleP, 2, Color.WHITE, Color.BLACK);
			g.setColor(Color.BLACK);
			for (int i = 0; i < displayMsgs.size(); i++) {
				g.drawString(displayMsgs.get(i), new Point(msgP, 0, (FONT.getSize() + 3) * i, 0));
			}
			
			accept.render(g);
			if (decline != null) decline.render(g);
		}
		
		private void fixMessage() {
			int maxWidth = DIM.width / FONT.getSize() - 2;
			String temp;

			for (int i = maxWidth; i < rawMsg.length(); i++) {
				if (rawMsg.charAt(i) == ' ') {
					temp = rawMsg.substring(i + 1, rawMsg.length());
					rawMsg = rawMsg.substring(0, i);
					rawMsg = rawMsg.concat("@");
					rawMsg = rawMsg.concat(temp);
					i += maxWidth;
				}
			}
		}
		
		public boolean attendAccept(Button.Action action, Physics.Vector side) {
			boolean attended = accept.attend(action, side);
			if (attended) Notifier.dismiss(title);
			return attended;
		}
		
		public boolean attendDecline(Button.Action action, Physics.Vector side) {
			if (decline == null) return false;
			
			boolean attended = decline.attend(action, side);
			if (attended) Notifier.dismiss(title);
			return attended;
		}
		
		public void setX(double x) {
			point.setX(x);
			
			if (decline == null) accept.setX(getX() + DIM.width / 2 - BUTTON_DIM.width / 2);
			else {
				accept.setX(getX() + 45);
				if (decline != null) decline.setX(getX() + 60 + BUTTON_DIM.width);
			}
		}
		
		public void setY(double y) {
			point.setY(y);
			accept.setY(getY() + DIM.height - BUTTON_DIM.height - 30);
			if (decline != null) decline.setY(getY() + DIM.height - BUTTON_DIM.height - 30);
		}
		
		public boolean isHovering() { return hover; }
		public String getTitle() { return title; }
		public Point getPoint() { return point;	}
		public double getX() { return point.getX(); }
		public double getY() { return point.getY(); }
		public void setDimension(Dimension dim) {}
		public Dimension getDimension() { return DIM; }
	}
	
	private static LinkedList<Notification> nList;
	
	public static void init() {
		nList = new LinkedList<Notification>();
	}
	
	public static void notify(String title, String msg, boolean declineButton) {
		nList.add(new Notification(title, msg, declineButton));
	}
	
	public static void dismiss(String msg) {
		nList.remove(getNotice(msg));
	}

	public static void update(double delta) {
		for (Notification n : nList) n.update(delta);
	}

	public static void render(ExtendedGraphics2D g) {
		for (Notification n : nList) n.render(g);
	}
	
	public static boolean attendAccept(String title, Button.Action action, Physics.Vector side) {
		Notification temp = getNotice(title.toUpperCase());
		if (temp == null) return false;
		else return temp.attendAccept(action, side);
	}
	
	public static boolean attendDecline(String title, Button.Action action, Physics.Vector side) {
		Notification temp = getNotice(title.toUpperCase());
		if (temp == null) return false;
		else return temp.attendDecline(action, side);
	}
	
	public static Notification getNotice(String title) {
		for (Notification n : nList) if (n.getTitle().equals(title)) return n;
		return null;
	}
	
	public static boolean isHovering() {
		for (Notification n : nList) if (n.isHovering()) return true;
		return false;
	}
}