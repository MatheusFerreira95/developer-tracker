package mestrado.matheus.teamtracker.domain;

public class NodeExplore {

	public final static String NODE_DEVELOPER = "Developer";
	public final static String NODE_DEVELOPER_COLOR = "#B00020";
	public final static String NODE_DEVELOPER_COLOR_TF = "darkorange";
	public final static String NODE_DEVELOPER_SYMBOL = "path://M27.7,24.31a11.92,11.92,0,0,0-7.76-8.65,6.5,6.5,0,1,0-7.88,0A11.92,11.92,0,0,0,4.3,24.31,3,3,0,0,0,7.23,28H24.77a3,3,0,0,0,2.92-3.69Z";

	public final static String NODE_PROJECT = "Project";
	public final static String NODE_PROJECT_COLOR = "#1B5E20";
	public final static String NODE_PROJECT_SYMBOL = "path://M18,4 L14,4 L14,2 L12,0 L8,0 L6,2 L6,4 L2,4 C0.9,4 0,4.9 0,6 L0,17 C0,18.1 0.9,19 2,19 L18,19 C19.1,19 20,18.1 20,17 L20,6 C20,4.9 19.1,4 18,4 L18,4 Z M12,4 L8,4 L8,2 L12,2 L12,4 L12,4 Z";

	public final static String NODE_FOLDER = "Folder";
	public final static String NODE_FOLDER_COLOR = "#FFD600";
	public final static String NODE_FOLDER_SYMBOL = "path://M0 4c0-1.1.9-2 2-2h7l2 2h7a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V4z";

	public final static String NODE_FILE = "File";
	public final static String NODE_FILE_COLOR = "#90A4AE";
	public final static String NODE_FILE_SYMBOL = "path://M19,3 L9.0085302,3 C7.8992496,3 7,3.89833832 7,5.00732994 L7,27.9926701 C7,29.1012878 7.89092539,30 8.99742191,30 L24.0025781,30 C25.1057238,30 26,29.1090746 26,28.0025781 L26,11 L21.0059191,11 C19.8980806,11 19,10.1132936 19,9.00189865 L19,3 L19,3 Z M20,3 L20,8.99707067 C20,9.55097324 20.4509752,10 20.990778,10 L26,10 L20,3 L20,3 Z M13,26 L9,22 L13,18 L13.6999998,18.6999998 L10.3999996,22 L13.6999998,25.3000002 L13,26 L13,26 Z M20,26 L24,22 L20,18 L19.3000002,18.6999998 L22.6000004,22 L19.3000002,25.3000002 L20,26 L20,26 Z M17,18 L15,26 L16,26 L18,18 L17,18 L17,18 Z";

	public String name;
	public String symbol;
	public String color;
	public String descrition;
	public String nodeType;
	//public int x;
	//public int y;

	public NodeExplore(String nodeType, String name, String descrition, Boolean isTruckFactor) {
		String nodeName = name != null ? name : NODE_PROJECT;
		String nodeDescrition = descrition != null ? descrition : nodeName;

		switch (nodeType) {
		case NODE_FOLDER:
			this.setValues(nodeName, NODE_FOLDER_SYMBOL, NODE_FOLDER_COLOR, nodeDescrition, nodeType);
			break;
		case NODE_FILE:
			this.setValues(nodeName, NODE_FILE_SYMBOL, NODE_FILE_COLOR, nodeDescrition, nodeType);
			break;
		case NODE_DEVELOPER:
			this.setValues(nodeName, NODE_DEVELOPER_SYMBOL, isTruckFactor ? NODE_DEVELOPER_COLOR_TF : NODE_DEVELOPER_COLOR, nodeDescrition, nodeType);
			break;

		default: // PROJECT
			this.setValues(nodeName, NODE_PROJECT_SYMBOL, NODE_PROJECT_COLOR, nodeDescrition, nodeType);
			break;
		}
	}

	private void setValues(String nodeName, String symbol, String color, String descrition, String nodeType) {
		this.name = nodeName;
		this.symbol = symbol;
		this.color = color;
		this.descrition = descrition;
		this.nodeType = nodeType;
		//x = 50;
		//y = 50;
	}

	@Override
	public boolean equals(Object obj) {
		NodeExplore node = (NodeExplore) obj;
		if (node.name.equals(this.name))
			return true;
		return false;
	}
}
