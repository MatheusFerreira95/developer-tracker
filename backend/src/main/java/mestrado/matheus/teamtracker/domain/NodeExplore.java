package mestrado.matheus.teamtracker.domain;

public class NodeExplore {

	public final static String NODE_DEVELOPER = "Developer";
	public final static String NODE_DEVELOPER_COLOR = "#B00020";
	public final static String NODE_DEVELOPER_SYMBOL = "path://M12.075,10.812c1.358-0.853,2.242-2.507,2.242-4.037c0-2.181-1.795-4.618-4.198-4.618S5.921,4.594,5.921,6.775c0,1.53,0.884,3.185,2.242,4.037c-3.222,0.865-5.6,3.807-5.6,7.298c0,0.23,0.189,0.42,0.42,0.42h14.273c0.23,0,0.42-0.189,0.42-0.42C17.676,14.619,15.297,11.677,12.075,10.812 M6.761,6.775c0-2.162,1.773-3.778,3.358-3.778s3.359,1.616,3.359,3.778c0,2.162-1.774,3.778-3.359,3.778S6.761,8.937,6.761,6.775 M3.415,17.69c0.218-3.51,3.142-6.297,6.704-6.297c3.562,0,6.486,2.787,6.705,6.297H3.415z";

	public final static String NODE_PROJECT = "Project";
	public final static String NODE_PROJECT_COLOR = "#1B5E20";
	public final static String NODE_PROJECT_SYMBOL = "path://M17.283,5.549h-5.26V4.335c0-0.222-0.183-0.404-0.404-0.404H8.381c-0.222,0-0.404,0.182-0.404,0.404v1.214h-5.26c-0.223,0-0.405,0.182-0.405,0.405v9.71c0,0.223,0.182,0.405,0.405,0.405h14.566c0.223,0,0.404-0.183,0.404-0.405v-9.71C17.688,5.731,17.506,5.549,17.283,5.549 M8.786,4.74h2.428v0.809H8.786V4.74z M16.879,15.26H3.122v-4.046h5.665v1.201c0,0.223,0.182,0.404,0.405,0.404h1.618c0.222,0,0.405-0.182,0.405-0.404v-1.201h5.665V15.26z M9.595,9.583h0.81v2.428h-0.81V9.583zM16.879,10.405h-5.665V9.19c0-0.222-0.183-0.405-0.405-0.405H9.191c-0.223,0-0.405,0.183-0.405,0.405v1.215H3.122V6.358h13.757V10.405z";

	public final static String NODE_FOLDER = "Folder";
	public final static String NODE_FOLDER_COLOR = "#FFD600";
	public final static String NODE_FOLDER_SYMBOL = "path://M17.927,5.828h-4.41l-1.929-1.961c-0.078-0.079-0.186-0.125-0.297-0.125H4.159c-0.229,0-0.417,0.188-0.417,0.417v1.669H2.073c-0.229,0-0.417,0.188-0.417,0.417v9.596c0,0.229,0.188,0.417,0.417,0.417h15.854c0.229,0,0.417-0.188,0.417-0.417V6.245C18.344,6.016,18.156,5.828,17.927,5.828 M4.577,4.577h6.539l1.231,1.251h-7.77V4.577z M17.51,15.424H2.491V6.663H17.51V15.424z";

	public final static String NODE_FILE = "File";
	public final static String NODE_FILE_COLOR = "#90A4AE";
	public final static String NODE_FILE_SYMBOL = "path://M15.475,6.692l-4.084-4.083C11.32,2.538,11.223,2.5,11.125,2.5h-6c-0.413,0-0.75,0.337-0.75,0.75v13.5c0,0.412,0.337,0.75,0.75,0.75h9.75c0.412,0,0.75-0.338,0.75-0.75V6.94C15.609,6.839,15.554,6.771,15.475,6.692 M11.5,3.779l2.843,2.846H11.5V3.779z M14.875,16.75h-9.75V3.25h5.625V7c0,0.206,0.168,0.375,0.375,0.375h3.75V16.75z";

	public String name;
	public String symbol;
	public String color;
	public String descrition;

	public NodeExplore(String nodeType, String name, String descrition) {
		String nodeName = name != null ? name : NODE_PROJECT;
		String nodeDescrition = descrition != null ? descrition : nodeName;

		switch (nodeType) {
		case NODE_FOLDER:
			this.setValues(nodeName, NODE_FOLDER_SYMBOL, NODE_FOLDER_COLOR, nodeDescrition);
			break;
		case NODE_FILE:
			this.setValues(nodeName, NODE_FILE_SYMBOL, NODE_FILE_COLOR, nodeDescrition);
			break;
		case NODE_DEVELOPER:
			this.setValues(nodeName, NODE_DEVELOPER_SYMBOL, NODE_DEVELOPER_COLOR, nodeDescrition);
			break;

		default: // PROJECT
			this.setValues(nodeName, NODE_PROJECT_SYMBOL, NODE_PROJECT_COLOR, nodeDescrition);
			break;
		}
	}

	private void setValues(String nodeName, String symbol, String color, String descrition) {
		this.name = nodeName;
		this.symbol = symbol;
		this.color = color;
		this.descrition = descrition;
	}
}
