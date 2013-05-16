package pa.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IOUtil 
{
    public static String readFileContent(String filepath)
    {
        String source = "";
        List<String> lines = readFileLines(filepath);
        for(String line : lines)
        {
            if(line.startsWith("#include"))
            {
                String[] inc = line.split(" ");
                String file = "null";
                if(inc.length == 2)
                {
                    file = inc[1].replace("\"", "");
                }
                source += readFileContent(file);
            }
            else
            {
                source += line + "\n";
            }
        }
        return source;
    }
    
    public static List<String> readFileLines(String filenpath) 
    {
        BufferedReader reader = null;
        List<String> lines = new ArrayList<String>();
        String errorMessage = null;
        try 
        {
            reader = new BufferedReader(new FileReader(filenpath));
            String line;
            while((line = reader.readLine()) != null) 
            {
                lines.add(line);
            }
        } catch (IOException ex) 
        {
            errorMessage = ex.getMessage();
        } finally 
        {
            try 
            {
                if(reader != null)
                {
                    reader.close();   
                }
            } catch (IOException ex) 
            {
                errorMessage = ex.getMessage();
            }
        }
        if(errorMessage != null)
        {
            throw new Error(errorMessage);
        }
        return lines;
    }
}
