package fe.up.pt.supermarket.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.databinding.ProductLayoutBinding;
import fe.up.pt.supermarket.models.Product;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<ListItem> productList;


    public ProductAdapter(Context context) {
        this.context = context;
        this.productList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //if (viewType == ListItem.TYPE_PRODUCT){
            ProductLayoutBinding lineupLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()), R.layout.product_layout, parent, false
            );

            return new ProductViewHolder(lineupLayoutBinding);
            //}
        //return new ProductViewHolder(lineupLayoutBinding);
    }

    @Override
    public int getItemViewType(int position) {
        return productList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);

        if (type == ListItem.TYPE_PRODUCT) {
            Product product = ((ProductItem) productList.get(position)).getProduct();
            ((ProductViewHolder) holder).productLayoutBinding.setProduct(product);
            //textView.setText(setProductInfo(textView, product));
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ProductLayoutBinding productLayoutBinding;

        public ProductViewHolder(@NonNull ProductLayoutBinding productLayoutBinding) {
            super(productLayoutBinding.getRoot());
            this.productLayoutBinding = productLayoutBinding;
        }
    }

    public void setProductsInfo(ArrayList<Product> productList) {
        this.productList = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            this.productList.add(new ProductItem(productList.get(i)));
        }

    }

    public abstract class ListItem {

        public static final int TYPE_DIVIDER = 0;
        public static final int TYPE_PRODUCT = 1;
        public static final int TYPE_CARD_DIVIDER = 2;
        abstract public int getType();
    }

    public class DividerItem extends ListItem {

        private String text;

        DividerItem(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        @Override
        public int getType() {
            return TYPE_DIVIDER;
        }

    }

    public class ProductItem extends ListItem {

        private Product product;

        ProductItem(Product event) {
            this.product = event;
        }

        public Product getProduct() {
            return this.product;
        }

        @Override
        public int getType() {
            return TYPE_PRODUCT;
        }

    }

    public class CardDividerItem extends ListItem {

        private String text;
        private Boolean isTop;

        CardDividerItem(String text, Boolean isTop) {
            this.text = text;
            this.isTop = isTop;
        }

        public String getText() {
            return this.text;
        }

        public Boolean isTop() {
            return this.isTop;
        }

        @Override
        public int getType() {
            return TYPE_CARD_DIVIDER;
        }

    }
}