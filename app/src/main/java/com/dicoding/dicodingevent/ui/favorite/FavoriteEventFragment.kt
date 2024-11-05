package com.dicoding.dicodingevent.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingevent.databinding.FragmentFavoriteEventBinding
import com.dicoding.dicodingevent.ui.EventAdapter
import com.dicoding.dicodingevent.ui.MainViewModel
import com.dicoding.dicodingevent.ui.ViewModelFactory
import kotlinx.coroutines.launch

class FavoriteEventFragment : Fragment() {
    private var _binding: FragmentFavoriteEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeFavorites()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter()

        adapter.setOnItemClickCallback { event ->
            val action = FavoriteEventFragmentDirections
                .actionNavigationFavoriteToDetailEventFragment(event.id)
            findNavController().navigate(action)
        }

        adapter.setOnFavoriteClickCallback { event ->
            viewModel.viewModelScope.launch {
                if (event.isFavorite) {
                    viewModel.deleteEvent(event)
                } else {
                    viewModel.setFavoriteEvent(event, true)
                }
            }
        }

        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@FavoriteEventFragment.adapter
        }
    }

    private fun observeFavorites() {
        binding.progressBar.visibility = View.VISIBLE

        viewModel.getFavoriteEvents().observe(viewLifecycleOwner) { favorites ->
            binding.progressBar.visibility = View.GONE

            if (favorites.isEmpty()) {
                binding.tvNoFavorites.visibility = View.VISIBLE
                binding.rvNews.visibility = View.GONE
            } else {
                binding.tvNoFavorites.visibility = View.GONE
                binding.rvNews.visibility = View.VISIBLE
                adapter.submitList(favorites)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}