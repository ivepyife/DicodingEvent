package com.dicoding.dicodingevent.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingevent.R
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.databinding.FragmentHomeBinding
import com.dicoding.dicodingevent.ui.EventAdapter
import com.dicoding.dicodingevent.ui.MainViewModel
import com.dicoding.dicodingevent.ui.ViewModelFactory
import kotlinx.coroutines.launch
import com.dicoding.dicodingevent.data.source.Result

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var activeEventsAdapter: EventAdapter
    private lateinit var pastEventsAdapter: EventAdapter

    private var isUpcomingLoaded = false
    private var isFinishedLoaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()

        // Fetch events using repository pattern
        viewModel.getAllEvents(1) // Active events (upcoming)
        viewModel.getAllEvents(0) // Past events (finished)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerViews() {
        // Initialize adapters with EventEntity
        activeEventsAdapter = EventAdapter()
        pastEventsAdapter = EventAdapter()

        // Set click listeners
        activeEventsAdapter.setOnItemClickCallback { event ->
            navigateToDetail(event)
        }
        activeEventsAdapter.setOnFavoriteClickCallback { event ->
            // Ubah status favorite di entity
            event.isFavorite = !event.isFavorite
            // Refresh adapter untuk update icon
            activeEventsAdapter.notifyDataSetChanged()

            // Proses di background
            viewModel.viewModelScope.launch {
                if (!event.isFavorite) { // Jika sekarang unfavorite (berarti sebelumnya favorite)
                    viewModel.deleteEvent(event)
                    viewModel.getAllEvents(1) // Reload setelah delete
                } else {
                    viewModel.setFavoriteEvent(event, true)
                }
            }
        }

        // Set click listeners for past events
        pastEventsAdapter.setOnItemClickCallback { event ->
            navigateToDetail(event)
        }
        pastEventsAdapter.setOnFavoriteClickCallback { event ->
            // Ubah status favorite di entity
            event.isFavorite = !event.isFavorite
            // Refresh adapter untuk update icon
            pastEventsAdapter.notifyDataSetChanged()

            // Proses di background
            viewModel.viewModelScope.launch {
                if (!event.isFavorite) { // Jika sekarang unfavorite (berarti sebelumnya favorite)
                    viewModel.deleteEvent(event)
                    viewModel.getAllEvents(0) // Reload setelah delete
                } else {
                    viewModel.setFavoriteEvent(event, true)
                }
            }
        }

        // Set adapters and layouts
        binding.rvActiveEvents.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = activeEventsAdapter
        }

        binding.rvPastEvents.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = pastEventsAdapter
        }
    }

    private fun observeViewModel() {
        // Observe upcoming events
        viewModel.getAllEvents(1).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    isUpcomingLoaded = false
                    showLoading(true)
                }
                is Result.Success -> {
                    isUpcomingLoaded = true
                    if (result.data.isNotEmpty()) {
                        activeEventsAdapter.submitList(result.data.take(5))
                        binding.tvErrorMessage.visibility = View.GONE
                    } else {
                        binding.tvErrorMessage.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = getString(R.string.tidak_ada_acara_aktif_yang_tersedia)
                    }
                    checkLoadingState()
                }
                is Result.Error -> {
                    isUpcomingLoaded = true
                    showErrorMessage(result.error)
                    checkLoadingState()
                }
            }
        }

        // Observe finished events
        viewModel.getAllEvents(0).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    isFinishedLoaded = false
                    showLoading(true)
                }
                is Result.Success -> {
                    isFinishedLoaded = true
                    if (result.data.isNotEmpty()) {
                        pastEventsAdapter.submitList(result.data.take(5))
                        binding.tvErrorMessage.visibility = View.GONE
                    } else {
                        binding.tvErrorMessage.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = getString(R.string.tidak_ada_acara_selesai_yang_tersedia)
                    }
                    checkLoadingState()
                }
                is Result.Error -> {
                    isFinishedLoaded = true
                    showErrorMessage(result.error)
                    checkLoadingState()
                }
            }
        }
    }

    private fun navigateToDetail(event: EventEntity) {
        val action = HomeFragmentDirections.actionNavigationHomeToDetailEventFragment(event.id)
        findNavController().navigate(action)
    }

    private fun checkLoadingState() {
        if (isUpcomingLoaded && isFinishedLoaded) {
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showErrorMessage(message: String) {
        binding.tvErrorMessage.text = message
        binding.tvErrorMessage.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}