package vrp.client;

import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geocode.DirectionQueryOptions;
import com.google.gwt.maps.client.geocode.DirectionResults;
import com.google.gwt.maps.client.geocode.Directions;
import com.google.gwt.maps.client.geocode.DirectionsCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geocode.Waypoint;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.PolyStyleOptions;
import com.google.gwt.user.client.ui.Grid;


import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;


import vrp.model.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class VRPGui implements EntryPoint {
	
	private MapWidget map;
	private int[][] distances;
	private int[] amounts;
	private int iCoord,jCoord;
	private int comDist;
	private int count;
	private TextArea output,input;
	private String[] adds;
	//current query sended to google maps
	private String currentQuery = "";
	
	private DialogBox exDialog;
	private TextBox txtNodeCount, txtdistanceConst,txtAmountConst, txtCarLimit;
	
	private Button btnCompDistanceMatrix,btnVisCities,btnVisPoint,btnGenerateRand, btnSweep,btnClark;
	
	private ListBox lst;
	
	private int routeCount = 0;
	
	private HorizontalPanel myPanel;
	private VerticalPanel topPanel;
    private DecoratedTabPanel midlePanel,actionsPanel;
    

	private String[] colors = {"red","blue","green","black","yellow","braun","gray"};
	private String[] colorStyles = {"red","green","blue","black","cyan","yellow","gray"};
	private int nCount;
	private Node[] nodes;
	private Canvas canvasMain;
	private Context2d context;
	
	private String htmlExample = "4<br/>0 355 1029 883<br/>355 0 1052 1035<br/>1029 1052 0 851<br/>883 1035 851 0<br/>" +
	"<br/>0;Prague;50.087811;14.42046<br/>13;Berlin, Germany;52.523405;13.4114<br/>25;Paris, France;48.856667;2.350987<br/>" +
	"15;Milano, Italy;45.463689;9.188141";
	
	public void createExDialog(String errMessage){
		exDialog = new DialogBox();
		VerticalPanel pn1 = new VerticalPanel();
		
		
		String htmlString = "<p><b>Exception while parsing the points, the input has to be like this:</b></p><i>" +  
			htmlExample + 
			"</i><p>First line: number of nodes</p>" +
			"<p>Distance matrix - the distance from each node to all others</p>" +
			"<p>Each line representing a node: [Demand - in 'units' how many does the city want] [Adress] [X coordinate] [Y coordinate]</p>" +
			"<p><b>Also you need to set the CAR LIMIT - how many 'units' can your car handle</p>"; 
		HTML htmlContent = new HTML(htmlString);
		
		
		pn1.add(htmlContent);
		pn1.add(new HTML("Details: <i>" + errMessage + "</i>"));
		Button btnClose = new Button("Close", new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				exDialog.hide();	
			}
			
		});
		
		pn1.add(btnClose);
		exDialog.add(pn1);
	}
	
	/**
	 * The callback called when the directions were computed
	 */
	DirectionsCallback callBack = new DirectionsCallback(){
		public void onFailure(int statusCode) {
			Window.alert("Problem with localization on MAP: " +statusCode + "\n Query: " + currentQuery);			
		}

		
		public void onSuccess(DirectionResults result) {
			double distMeters = result.getDistance().inMeters();
			
			if(nodes[iCoord].x==0 || nodes[iCoord].y==0){
				List<Placemark> placeMarks = result.getPlacemarks();
				Placemark pm1 =  placeMarks.get(0);
				LatLng pm1Point = pm1.getPoint();
				
				nodes[iCoord].x = pm1Point.getLatitude();
				nodes[iCoord].y = pm1Point.getLongitude();
			}
			
	        comDist = (int)distMeters/1000;
	        distances[iCoord][jCoord] = comDist;
	        distances[jCoord][iCoord] = comDist;
	        
	        Timer t = new Timer(){

				@Override
				public void run() {
					processNext();
					this.cancel();
				}
	        };
	        t.schedule(500);
	        //processNext();
		}
	};
	
	DirectionsCallback visualiseCallBack = new DirectionsCallback(){

		@Override
		public void onFailure(int statusCode) {
			Window.alert(""+statusCode);			
		}

		@Override
		public void onSuccess(DirectionResults result) {
			PolyStyleOptions style = PolyStyleOptions.getInstance();
			style.setColor(colors[routeCount%colors.length]);
			result.getPolyline().setStrokeStyle(style);
			routeCount++;
			
			
		}
	};
	
	public void loadPoints(){
		//clear the points which are on canvas
		
		String in = input.getText();
		String[] inField = in.split("\n");
		
		nCount = Integer.parseInt(inField[0]);
		distances = new int[nCount][nCount];
		
		//nactu tabulku vzdalenosti
		for(int i=0;i<nCount;i++){
			String line = inField[i+1];
			String[] inDist = line.split(" ");
			for(int k=0;k<inDist.length;k++){
				int dis = Integer.parseInt(inDist[k]);
				distances[i][k] = dis;
			}
		}
		
		
		//nactu mnozstvi objednaneho zbozi
		//adresy a souradnice
		
		nodes = new Node[nCount];
		amounts = new int[nCount];
		adds = new String[nCount];
		
		for(int i=0;i<nCount;i++){
			String nodeInfo = inField[nCount+2+i];
			String[] info = nodeInfo.split(";");
		
			amounts[i] = Integer.parseInt(info[0]);
			adds[i] = info[1];
			
			Node n = new Node(i);
			n.amount = amounts[i];
			n.add = info[1];
			
			n.x = Double.parseDouble(info[2]);
			n.y = Double.parseDouble(info[3]);
			nodes[i] = n;
		}
		
		drawPoints();
	}
	
	
	ClickHandler computeDistancesHandler = new ClickHandler(){

		@Override
		public void onClick(ClickEvent event) {
			
			/*load the cities to the list*/
			lst.clear();
			
			String text = input.getText();
			String[] cities = text.split("\n");
			
			nodes = new Node[cities.length];
			for(int i=0;i<cities.length;i++){
				
				
				lst.insertItem(cities[i], i);
				
				
				Node n = new Node(i);
				
				String desc[] = cities[i].split(":");
				if(desc.length==2){
					n.add = desc[0];
					n.amount = Integer.parseInt(desc[1].trim());
					nodes[i] = n;
				}else{
					output.setText("Error in line: " + cities[i]);
					return;
				}
			}
			
			count = lst.getItemCount();
			distances = new int[count][count];
			
			iCoord = 0;
			jCoord = iCoord+1;
			
			String city1 = lst.getItemText(iCoord);
			String city2 = lst.getItemText(jCoord);
			
			DirectionQueryOptions opts = new DirectionQueryOptions(map);
			
			currentQuery = "from: " + city1 + " to: " + city2; // 500 Memorial Dr, Cambridge, MA to: 4 Yawkey Way, Boston, MA";
		    Directions.load(currentQuery,opts,callBack);
		}
		
	};
	
	ClickHandler generateRandHand = new ClickHandler(){

		@Override
		public void onClick(ClickEvent event) {
			try{
				int amountConst = Integer.parseInt(txtAmountConst.getText());
				int multConst = Integer.parseInt(txtdistanceConst.getText());
				int nodeCount = Integer.parseInt(txtNodeCount.getText());
				String inputString = MyUtils.generateRandomNodes(nodeCount, multConst, amountConst);
				input.setText(inputString);
				//select the GRAP tab for visualising the points
				midlePanel.selectTab(1);
				loadPoints();
				
			}catch(Exception ex){
				final DialogBox db = new DialogBox();
				VerticalPanel dialogContent = new VerticalPanel();
				
				db.setTitle("Error while generating random matrix");
				HTML label = new HTML("<p>Error while generating random matrix, details:</p>");
				// Add a close button at the bottom of the dialog
			    Button closeButton = new Button("Close",
			        new ClickHandler() {
			          public void onClick(ClickEvent event) {
			            db.hide();
			          }
			        });
			    dialogContent.add(label);
			    dialogContent.add(closeButton);
			    dialogContent.add(new HTML("<i>" + ex.toString() + "</i>"));
				db.add(dialogContent);
				db.center();
				db.show();
			}
		}
	};
	ClickHandler visualiseTSPHandler = new ClickHandler(){

		@Override
		public void onClick(ClickEvent event) {
			String text = output.getText();
			String[] input = text.split("\n");
			int routesCount = Integer.parseInt(input[0]);
			
			for(int i=routesCount+1;i<(2*routesCount+1);i++){
				String citiesString = input[i];
				final String[] cities = citiesString.split("->");
				
				final Waypoint[] wPoints = new Waypoint[cities.length];
				
				//projdu mesta a zobrazim je
				for(int j=0;j<cities.length;j++){
					
					String cityAddress = cities[j];
					
					Waypoint w = new Waypoint(cityAddress);
					wPoints[j] = w;
					
				}
				
				Timer t = new Timer(){

					@Override
					public void run() {
						
						DirectionQueryOptions opts = new DirectionQueryOptions(map);
						Directions.loadFromWaypoints(wPoints, opts,visualiseCallBack);
						this.cancel();
					}
		        };
		        
		        t.schedule(400);
				//DirectionQueryOptions opts = new DirectionQueryOptions(map);
				//Directions.loadFromWaypoints(wPoints, opts,visualiseCallBack);
				
			}
		}
		
	};
	
	ClickHandler sweepClickHandler = new ClickHandler(){

		@Override
		public void onClick(ClickEvent event) {
			try{
				loadPoints();
				int carLimit = Integer.parseInt(txtCarLimit.getText());
				boolean loaded = VRPProgram.loadData(nodes,nCount,distances,amounts,adds,carLimit);
				if(loaded){
					String outputSweep = VRPProgram.sweep();
					output.setText(outputSweep);
				}
			}catch(Exception ex){
				if(exDialog==null){
					createExDialog(ex.toString());
				}
				exDialog.center();
				exDialog.show();
			}
		}
		
	};
	
	ClickHandler clarkClickHandler = new ClickHandler(){

		@Override
		public void onClick(ClickEvent event) {
			try{
				loadPoints();
				int carLimit = Integer.parseInt(txtCarLimit.getText());
				boolean loaded = VRPProgram.loadData(nodes,nCount,distances,amounts,adds,carLimit);
				if(loaded){
					String outputClark = VRPProgram.clarkWright();
					output.setText(outputClark);
				}
			}catch(Exception ex){
				if(exDialog==null){
					createExDialog(ex.toString());
				}
				exDialog.show();
				exDialog.center();
			}
		}
		
	};
	
	public void processNext(){
		if(iCoord<count-1){
			if(jCoord<count-1){
				jCoord++;
			}else{
				iCoord++;
				jCoord=0;
			}
			
			String city1 = lst.getItemText(iCoord);
			String city2 = lst.getItemText(jCoord);
			
			
			DirectionQueryOptions opts = new DirectionQueryOptions(map);
			currentQuery = "from: " + city1 + " to: " + city2; // 500 Memorial Dr, Cambridge, MA to: 4 Yawkey Way, Boston, MA";
		    Directions.load(currentQuery,opts,callBack);
		}else{
			printOut();
		}
	}
	
	public void visualiseGRAPHResult(){
		
		drawPoints();
		
		String in = output.getText();
		String[] routes = in.split("\n");
		
		int inputRouteCount = Integer.parseInt(routes[0]);
		
		double movX = canvasMain.getOffsetWidth() / 2;// getCoordWidth()/2-nodes[0].x;
		double movY = canvasMain.getOffsetHeight()/2-nodes[0].y;
		
		
		
		for(int j=1;j<inputRouteCount+1;j++){
			String r = routes[j];
			String[] cities = r.split(" ");
			context.setStrokeStyle(colorStyles[j%colorStyles.length]);
			
			context.beginPath();
			double x = nodes[0].x + movX;
			double y = nodes[0].y + movY;
			
			
		    context.moveTo(x,y);
		    
		    for(int i=1;i<cities.length;i++){
		    	int nodeID = Integer.parseInt(cities[i]);
		    	
		    	x = nodes[nodeID].x + movX;
		    	y = canvasMain.getOffsetHeight()- nodes[nodeID].y - movY;
		        context.lineTo(x,y);
		        
		    }
		    context.closePath();
		    context.stroke();
		}
	}
	
	ClickHandler visualizePointsHandler = new ClickHandler(){

		@Override
		public void onClick(ClickEvent event) {
			visualiseGRAPHResult();
		}
		
	};

	public void drawPoints(){
		//clear the canvas
		//TODO: clear the canvas
		
		//midlePanel.selectTab(1);
		context.setStrokeStyle("");
		double movX = canvasMain.getOffsetWidth()/2-nodes[0].x;
		double movY = canvasMain.getOffsetHeight()/2-nodes[0].y;
		
		
		double x = nodes[0].x + movX;
		double y = nodes[0].y + movY;
		context.moveTo(x, y);
		
	    context.moveTo(x,y);
		for(int i=1;i<nodes.length;i++){
			x = nodes[i].x + movX;
	    	y = canvasMain.getOffsetHeight()- nodes[i].y - movY;
	    	context.rect(x, y, 5, 5);
	
		}
		context.stroke();
	}
	
	@Override
	public void onModuleLoad() {
	
		input = new TextArea();
		input.setHeight("200px");
		input.setWidth("500px");
		
		
		String noHTMLString = htmlExample.replaceAll("<br/>","\n");
		
		
		
		input.setText(noHTMLString);
		
		output = new TextArea();
		output.setHeight("150px");
		output.setWidth("500px");
		
	    lst = new ListBox();
	    
	    
	    btnVisCities = new Button("Visualise on MAP");
	    btnVisCities.addClickHandler(visualiseTSPHandler);
	    
	    
	    btnVisPoint = new Button("Visualise on GRAPH");
	    btnVisPoint.addClickHandler(visualizePointsHandler);
	    			    
	    btnGenerateRand = new Button("Generate Random");
	    btnGenerateRand.addClickHandler(generateRandHand);
	    
	    btnSweep = new Button("Sweep Alg.");
	    btnSweep.addClickHandler(sweepClickHandler);
	    
	    btnClark = new Button("Clark & Wright Alg.");
	    btnClark.addClickHandler(clarkClickHandler);
	    
	    Grid genGrid = new Grid(4,2);
	    
	    HTML htmlAmount = new HTML("Amount mult const:");
	    txtAmountConst = new TextBox();
	    txtAmountConst.setText("15");
	    txtAmountConst.setFocus(true);
	    txtAmountConst.setWidth("40px");
	    genGrid.setWidget(0, 0, htmlAmount);
	    genGrid.setWidget(0, 1, txtAmountConst);
	    
	    HTML htmlDistance = new HTML("Distance mult const:");
	    txtdistanceConst = new TextBox();
	    txtdistanceConst.setText("200");
	    txtdistanceConst.selectAll();
	    txtdistanceConst.setWidth("40px");
	    
	    genGrid.setWidget(1,0, htmlDistance);
	    genGrid.setWidget(1,1, txtdistanceConst);
	    
	    HTML htmlNodeCount = new HTML("Node count:");
	    txtNodeCount = new TextBox();
	    txtNodeCount.setText("30");
	    txtNodeCount.selectAll();
	    txtNodeCount.setWidth("40px");
	    genGrid.setWidget(2, 0, htmlNodeCount);
	    genGrid.setWidget(2, 1, txtNodeCount);
	    
	    
	    actionsPanel = new DecoratedTabPanel();
	    
	    VerticalPanel generateVerticalPanel = new VerticalPanel();
	    generateVerticalPanel.add(genGrid);
	    generateVerticalPanel.add(btnGenerateRand);
	    
	    
	    actionsPanel.add(generateVerticalPanel,"Generate random graph");
	    
	    txtCarLimit = new TextBox();
	    txtCarLimit.setText("40");
	    
	    VerticalPanel vrpVerticalPanel = new VerticalPanel();
	    vrpVerticalPanel.add(new HTML("The CAR LIMIT:"));
	    vrpVerticalPanel.add(txtCarLimit);
	    vrpVerticalPanel.add(btnSweep);
	    vrpVerticalPanel.add(btnClark);
	    vrpVerticalPanel.add(btnVisCities);
	    vrpVerticalPanel.add(btnVisPoint);
	    actionsPanel.add(vrpVerticalPanel,"Solve VRP");
	    
	    VerticalPanel computeVerticalPanel = new VerticalPanel();
	    
	    btnCompDistanceMatrix = new Button("Compute Distance Matrix");
	    btnCompDistanceMatrix.addClickHandler(computeDistancesHandler);
	    computeVerticalPanel.add(btnCompDistanceMatrix);
	    computeVerticalPanel.add(lst);
	    actionsPanel.add(computeVerticalPanel,"Comp. Dist. Matrix");
	    actionsPanel.selectTab(0);
	    
	    topPanel = new VerticalPanel();
	    topPanel.add(actionsPanel);
	    topPanel.add(new HTML("INPUT:"));
	    topPanel.add(input);
	    topPanel.add(new HTML("OUTPUT:"));
	    topPanel.add(output);
	    
	    
	    midlePanel = new DecoratedTabPanel();
	    midlePanel.setHeight("500px");
	    midlePanel.setWidth("600px");
	    midlePanel.setAnimationEnabled(true);
	    
	   
	    
	    map = new MapWidget();
	    map.setHeight("500px");
	    map.setWidth("600px");
	    
	    VerticalPanel pn2 = new VerticalPanel();
	    pn2.add(map);
	    	    
	    midlePanel.add(pn2,"Map");
	    
	    
	    canvasMain = Canvas.createIfSupported();
	    canvasMain.setWidth("300px");
	    canvasMain.setHeight("300px");
	    
	    context = canvasMain.getContext2d();
	    context.setStrokeStyle("green");
	    context.setLineWidth(1);
	    
	    VerticalPanel pn1 = new VerticalPanel();
	    pn1.add(canvasMain);
	    
	    midlePanel.add(pn1,"Graph");
	    midlePanel.selectTab(0);
	    
	    myPanel = new HorizontalPanel();
	    myPanel.add(topPanel);
	    myPanel.add(midlePanel);
	    myPanel.setSpacing(3);
	    RootPanel.get("mapsTutorial").add(myPanel);
	}
	public void printOut(){
		//vypisu
		StringBuilder sb = new StringBuilder();
		
		sb.append(count);
		for(int i=0;i<count;i++){
			sb.append("\n");
			for(int j=0;j<count;j++){
				if(i==j){
					sb.append(0);
				}else{
					if(distances[i][j] != 0){
						sb.append(distances[i][j]);
					}else{
						sb.append(distances[j][i]);
					}
				}
				sb.append(" ");
			}
		}
		
		sb.append("\n");
		sb.append("\n");
		
		for(int j=0;j<nodes.length;j++){
			Node n = nodes[j];
			sb.append(n.amount + ";" + n.add + ";" + n.x + ";" + n.y);
			sb.append("\n");
		}
		
		//input.setText(sb.toString());
		output.setText(sb.toString());
	}
}
