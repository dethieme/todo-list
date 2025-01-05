package de.thieme.util;

import android.widget.ImageView;
import org.dieschnittstelle.mobile.android.skeleton.R;
import de.thieme.model.ToDo;

abstract public class ImageViewUtil {

    public static void setFavouriteIcon(ImageView imageView, ToDo toDo) {
        imageView.setImageResource(toDo.isFavourite()
                ? R.drawable.baseline_star_24
                : R.drawable.baseline_star_border_24);
    }
}
