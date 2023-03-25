package ooo.github.io.es.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ooo.github.io.es.anno.Id;
import ooo.github.io.es.anno.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kaiqin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QyDocument {

    @Id
    private String qyId;

    @Type(type = "keyword")
    private String qyMc;

    private String jd;

    private String wd;

    private String location;

    public QyDocument(String qyId, String qyMc) {
        this.qyId = qyId;
        this.qyMc = qyMc;
    }


    public static List<QyDocument> mock() {
        List<QyDocument> result = new ArrayList<>();
        for (int i = 10; i < 15; i++) {
            QyDocument qyDocument = new QyDocument(i + "", "武汉无敌战队企业", "108.121307", "29.992445", "29.992445,108.121307");
            result.add(qyDocument);
        }
        return result;
    }
}
