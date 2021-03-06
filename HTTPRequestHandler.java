import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class HTTPRequestHandler extends AbstractHandler {

    private ChessBoard board;

    public HTTPRequestHandler(ChessBoard board)
    {
        this.board = board;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {                 
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        if (request.getRequestURI().equals("/favicon.ico")) return; // SKIP FAVICON REQUESTS;

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date dateobj = new Date();
        System.out.println();

        String requestText = "[ " + request.getRemoteAddr()  + "  |  " + df.format(dateobj) + "  |  ";
        requestText += request.getMethod() + " ] \t " + request.getRequestURI() + " \t ";

        if (request.getQueryString() != null)
        {

            String start = null;
            String end = null;
            String setposition = null;
            int setvalue = 0;
            String setunmoved = null;

            for(String q : request.getQueryString().split("&"))
            {
                if (q.contains("=")) 
                {
                    String variable = q.split("=")[0];
                    String value = q.split("=")[1];
                    requestText += "   " + variable + " = " + value; 

                    if (variable.equals("start")) start = value;
                    if (variable.equals("end")) end = value;                    
                    if (variable.equals("position")) setposition = value;
                    try { if (variable.equals("value")) setvalue = Integer.parseInt(value); }
                    catch (Exception ex) { System.out.println("Sync error - Can't convert " + value + " to integer."); }
                    if (variable.equals("unmoved")) setunmoved = value;
                }
                else               
                {
                    requestText += "   Invalid query string component (" + q + ")";
                }
            }

            if (request.getRequestURI().contains("move"))
            {
                if (start != null && end != null) board.doMove(start, end);
            }

            if (request.getRequestURI().contains("set"))
            {
                if (setposition != null && setunmoved != null) board.setSquare(setposition, setvalue, setunmoved.toLowerCase().equals("true"));
            }

        }
        else
        {
            requestText += "   No query string supplied";
        }
        System.out.println(requestText);

        response.getWriter().println(requestText);

        baseRequest.setHandled(true);
    }

    public static String getMyNetworkAdapter() throws SocketException
    {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) 
        {
            NetworkInterface iface = interfaces.nextElement();

            if (iface.isLoopback() || !iface.isUp()) continue; // filters out 127.0.0.1 and inactive interfaces

            Enumeration<InetAddress> addresses = iface.getInetAddresses();            

            if (iface.getDisplayName().startsWith("wlan0")) continue;

            while(addresses.hasMoreElements())             
            {                
                InetAddress addr = addresses.nextElement();
                if (!(addr instanceof Inet4Address)) continue;
                return (addr.getHostAddress());
            }
        }
        return null;
    }
}
