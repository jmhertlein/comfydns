package cafe.josh.comfydns.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Resources {
    public static Stream<String> readLines(InputStream is) {
        Scanner scan = new Scanner(is);
        List<String> lines = new ArrayList<>();
        while(scan.hasNextLine()) {
            lines.add(scan.nextLine());
        }

        return lines.stream();
    }
}
