/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import Utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author alumne
 */
@WebServlet(name = "deletePatient", urlPatterns = {"/deletePatient"})
@MultipartConfig
public class deletePatient extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;}
        
        String mpegfile = request.getParameter("mpegfile");
        String dg_id = request.getParameter("dg_id");
        String dt_id = request.getParameter("dt_id");
        String patient_id = request.getParameter("patient_id");
        
        System.out.println("Valores que llegan al servlet deletePatient: " + patient_id + " (patient_id)");
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
        String res = "",aux;
        try {
            URL url = new URL(props.getProperty("workflowUrl") + "/api/v1/deletePatient?dt_id=" + dt_id + "&dg_id=" + dg_id + "&mpegfile=" + mpegfile + "&patient_id=" + patient_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , "Bearer "+Utils.getToken());
            conn.setRequestMethod("DELETE");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            
            while((aux = br.readLine()) != null)
                res += aux;
            //System.out.println(res);
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(deleteDataset.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(deleteDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(res);
        //TODO fix redirect to referer
        request.setAttribute("mpegfile", mpegfile);
        request.setAttribute("dg_id", dg_id);
        if(!res.isEmpty())request.setAttribute("success","true");
        else request.setAttribute("success","false");
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/getDatasets");
        dispatcher.forward(request, response);
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
