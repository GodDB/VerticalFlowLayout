package com.godgod.customlayoutmanager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.godgod.customlayoutmanager.databinding.ActivityMainBinding
import com.godgod.customlayoutmanager.databinding.ViewholderTestBinding
import com.google.android.flexbox.FlexDirection.ROW
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

       /* binding.rv.layoutManager = FlexboxLayoutManager(this)*/
        binding.rv.layoutManager = VerticalFlowLayoutManager()
   /*     binding.rv.layoutManager = LinearLayoutManager(this)*/
        binding.rv.adapter = TestAdapter()
        (binding.rv.adapter as TestAdapter).setAll(createViewDatas())
    }

    private fun createViewDatas(): List<String> {
        val list: MutableList<String> = mutableListOf()

        for (i in 0 until 1000) {
            if(i % 10 == 0) {
                list.add("godgodgodogdogodgodgodgodogodgodgodogdo $i")
            } else {
                list.add("godgod $i")
            }
        }

        return list
    }
}


class TestAdapter : RecyclerView.Adapter<TestViewHolder>() {

    private val items: MutableList<String> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        Log.e("godgod", "onCreateVH")
        val view = ViewholderTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        Log.e("godgod", "onBindVH  $position")
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setAll(list: List<String>) {
        this.items.clear()
        this.items.addAll(list)
        this.notifyDataSetChanged()
    }

}

class TestViewHolder(private val binding: ViewholderTestBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(value: String) {
        binding.tvItem.text = value
    }

}