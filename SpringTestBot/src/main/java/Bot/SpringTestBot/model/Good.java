package Bot.SpringTestBot.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity(name="goodsDataTable")
public class Good {

    @Id
    private Long iD;

    private String goodName;

    private String goodDescription;

    private String price;

    private String sellerUserName;

    @Lob
    private byte[] imageBytes;

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public Long getiD() {
        return iD;
    }

    public void setiD(Long iD) {
        this.iD = iD;
    }

    public String getGoodName() {
        return goodName;
    }

    public void setGoodName(String goodName) {
        this.goodName = goodName;
    }

    public String getGoodDescription() {
        return goodDescription;
    }

    public void setGoodDescription(String goodDescription) {
        this.goodDescription = goodDescription;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSellerUserName() {
        return sellerUserName;
    }

    public void setSellerUserName(String sellerUserName) {
        this.sellerUserName = sellerUserName;
    }

    public Good() {
    }

    public Good(String goodName, String goodDescription, String price, String sellerUserName) {
        this.goodName = goodName;
        this.goodDescription = goodDescription;
        this.price = price;
        this.sellerUserName = sellerUserName;
    }
}
