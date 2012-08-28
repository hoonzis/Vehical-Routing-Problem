<%@ Page Language="C#" AutoEventWireup="true" CodeFile="vrp.aspx.cs" Inherits="_about" %>

<asp:Content ID="Content1" ContentPlaceHolderID="cphBody" Runat="Server">
<script type="text/javascript" language="javascript" src="vrpgui/vrpgui.nocache.js"></script>
    <script type="text/javascript">
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-12336393-5']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>
	<h2>Vehicle Routing Problem</h2>
	<p>This is a small utility which solves the Vehicle Routing Problem. This tool was written in JAVA with usage of GWT API. This tool implements two basic algorithms:</p>
	<ul>
		<li>Clark & Wright Savings Algorithm</li>
		<li>The Sweep Algorithm</li>
	</ul>	
  	<p><a href="http://honga.super6.cz/2010/05/vehicle-routing-problem.html">Quick HowTo for this tool and the source code can be found on my blog.</a></p>
    <div id="mapsTutorial"></div>
</asp:Content>