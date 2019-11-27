package fe.up.pt.supermarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.databinding.ProductLayoutBinding;
import fe.up.pt.supermarket.databinding.TransactionLayoutBinding;
import fe.up.pt.supermarket.models.Product;
import fe.up.pt.supermarket.models.Transaction;

public class TransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<ListItem> transactionList;


    public TransactionsAdapter(Context context) {
        this.context = context;
        this.transactionList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //if (viewType == ListItem.TYPE_PRODUCT){
        TransactionLayoutBinding transactionLayoutBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.transaction_layout, parent, false
        );

        return new TransactionViewHolder(transactionLayoutBinding);
        //}
        //return new ProductViewHolder(lineupLayoutBinding);
    }

    public void clear() {
        int size = transactionList.size();
        transactionList.clear();
        notifyItemRangeRemoved(0, size);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return transactionList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);

        if (type == ListItem.TYPE_TRANSACTION) {
            Transaction transaction = ((TransactionItem) transactionList.get(position)).getTransaction();
            ((TransactionViewHolder) holder).transactionLayoutBinding.setTransaction(transaction);
            //textView.setText(setProductInfo(textView, product));
        }
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TransactionLayoutBinding transactionLayoutBinding;

        public TransactionViewHolder(@NonNull TransactionLayoutBinding transactionLayoutBinding) {
            super(transactionLayoutBinding.getRoot());
            this.transactionLayoutBinding = transactionLayoutBinding;
        }
    }

    public void setTransactionInfo(ArrayList<Transaction> transactionList) {
        this.transactionList = new ArrayList<>();
        for (int i = 0; i < transactionList.size(); i++) {
            this.transactionList.add(new TransactionItem(transactionList.get(i)));
        }

    }

    public abstract class ListItem {

        public static final int TYPE_DIVIDER = 0;
        public static final int TYPE_TRANSACTION = 1;
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

    public class TransactionItem extends ListItem {
        private Transaction transaction;

        TransactionItem(Transaction event) {
            this.transaction = event;
        }

        public Transaction getTransaction() {
            return this.transaction;
        }

        @Override
        public int getType() {
            return TYPE_TRANSACTION;
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
