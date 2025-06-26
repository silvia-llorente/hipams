<%@page import="Utils.Utils"%>
<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<% PrintWriter print = response.getWriter();//System.out.println(request.getAttribute("file_id"));
String mpegfile = null, dg_id = null, dt_id = null;
String referer =  new StringBuilder(new StringBuilder().append(request.getHeader("referer")).reverse().toString().split("/")[0]).reverse().toString();
if(request.getAttribute("file_id") != null) mpegfile = request.getAttribute("file_id").toString();
else mpegfile = request.getParameter("file_id");
if(request.getAttribute("dg_id") != null) dg_id = request.getAttribute("dg_id").toString();
else dg_id = request.getParameter("dg_id");
if(request.getAttribute("dt_id") != null) dt_id = request.getAttribute("dt_id").toString();
else dt_id = request.getParameter("dt_id");
if(mpegfile == null || dg_id == null || dt_id == null) response.sendRedirect(referer);
print.println(Prints.printHead("Add Access Unit")+
"<div class=\"container\">\n" +
"    <h1>Add Access Unit</h1>\n"); 
if(request.getAttribute("success") != null && request.getAttribute("success").equals("true")) print.println("    <div class=\"card-panel green white-text\" >Access Unit added successfully</div>\n"); 
else if(request.getAttribute("success") != null && request.getAttribute("success").equals("false")) print.println("    <div class=\"card-panel red white-text\" >Error adding access unit</div>\n");
print.println("    <div class=\"row\">\n" +
"        <form  method=\"post\" action=\"addPatient\" enctype=\"multipart/form-data\" class=\"col s12\">\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select FHIR Metadadata</span>\n" +
"                    <input type=\"file\" name=\"patient_data\" accept=\"text\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select FHIR Protection</span>\n" +
"                    <input type=\"file\" name=\"patient_pr\" accept=\"text/xml\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <input type=\"hidden\" name=\"mpegfile\" value=\""+mpegfile+"\" />\n" +
"            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\" />\n" +
"            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\" />\n" +
"            <div class=\"input-field\">\n" +
"                <button class=\"btn waves-effect waves-light\" type=\"submit\">\n" +
"                    <i class=\"material-icons dp48\">file_upload</i> Create FHIR resource\n" +
"                </button>\n" +
"            </div>\n" +
"        </form>\n" +
"        <form action=\""+referer+"\" method=\"POST\" class=\"col s12\">\n" +
"            <input type=\"hidden\" name=\"file_id\" value=\""+mpegfile+"\"/>\n" +
"            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\" />\n" +
"            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\" />\n" +
"            <button class=\"btn waves-effect waves-light\" >Go Back</button>\n" +
"        </form>\n" +
"    </div>\n" +
"</div>\n" +
"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
"</body>\n" +
"</html>");
%>