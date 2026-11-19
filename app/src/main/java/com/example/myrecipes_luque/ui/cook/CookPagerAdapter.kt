package com.example.myrecipes_luque.ui.cook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipes_luque.R
import com.example.myrecipes_luque.databinding.ItemCookIngredientBinding
import com.example.myrecipes_luque.databinding.ItemCookPrepPageBinding
import com.example.myrecipes_luque.databinding.ItemCookStepPageBinding
import com.example.myrecipes_luque.util.ThumbLoader

sealed class CookPage {
    data class Prep(
        val lines: List<String>,
        val names: List<String>,
    ) : CookPage()

    data class Step(
        val number: Int,
        val total: Int,
        val body: String,
    ) : CookPage()
}

class CookPagerAdapter(
    private val pages: List<CookPage>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = pages.size

    override fun getItemViewType(position: Int): Int = when (pages[position]) {
        is CookPage.Prep -> TYPE_PREP
        is CookPage.Step -> TYPE_STEP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PREP -> PrepViewHolder(
                ItemCookPrepPageBinding.inflate(inflater, parent, false),
            )
            else -> StepViewHolder(
                ItemCookStepPageBinding.inflate(inflater, parent, false),
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val page = pages[position]) {
            is CookPage.Prep -> (holder as PrepViewHolder).bind(page)
            is CookPage.Step -> (holder as StepViewHolder).bind(page)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PrepViewHolder) {
            holder.clearImages()
        }
        super.onViewRecycled(holder)
    }

    class PrepViewHolder(
        private val binding: ItemCookPrepPageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: CookPage.Prep) {
            val container = binding.containerCookIngredients
            if (container.childCount != page.lines.size) {
                container.removeAllViews()
                val inflater = LayoutInflater.from(container.context)
                page.lines.forEachIndexed { index, line ->
                    val item = ItemCookIngredientBinding.inflate(inflater, container, false)
                    item.checkIngredient.text = line
                    val name = page.names.getOrNull(index) ?: line
                    ThumbLoader.loadIngredient(item.imageIngredient, name)
                    container.addView(item.root)
                }
            }
        }

        fun clearImages() {
            val container = binding.containerCookIngredients
            for (i in 0 until container.childCount) {
                val image = container.getChildAt(i).findViewById<android.widget.ImageView>(
                    R.id.image_ingredient,
                )
                if (image != null) {
                    ThumbLoader.clear(image)
                }
            }
        }
    }

    class StepViewHolder(
        private val binding: ItemCookStepPageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: CookPage.Step) {
            binding.textCookBadge.text = binding.root.resources.getString(
                R.string.step_index_format,
                page.number,
            )
            binding.textCookPhase.text = binding.root.resources.getString(
                R.string.cook_step_title,
                page.number,
                page.total,
            )
            binding.textCookStep.text = page.body
        }
    }

    private companion object {
        const val TYPE_PREP = 0
        const val TYPE_STEP = 1
    }
}
