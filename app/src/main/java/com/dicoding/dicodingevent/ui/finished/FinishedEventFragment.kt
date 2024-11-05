package com.dicoding.dicodingevent.ui.finished

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingevent.R
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.databinding.FragmentItemListBinding
import com.dicoding.dicodingevent.ui.EventAdapter
import com.dicoding.dicodingevent.ui.MainViewModel
import com.dicoding.dicodingevent.ui.ViewModelFactory
import com.dicoding.dicodingevent.data.source.Result
import kotlinx.coroutines.launch

class FinishedEventFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        viewModel.getAllEvents(0)
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter()

        eventAdapter.setOnItemClickCallback { event ->
            navigateToDetail(event)
        }

        eventAdapter.setOnFavoriteClickCallback { event ->
            viewModel.viewModelScope.launch {
                if (event.isFavorite) {
                    viewModel.deleteEvent(event)
                    observeViewModel()
                } else {
                    viewModel.setFavoriteEvent(event, true)
                }
            }
        }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupSearchView() {
        binding.searchView.setupWithSearchBar(binding.searchBar)
        binding.searchView.editText.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchView.text.toString()
            performSearch(query)
            false
        }
    }

    private fun performSearch(query: String) {
        // Use repository pattern for search
        viewModel.getAllEvents(0).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    val filteredEvents = result.data.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                    updateEventList(filteredEvents)
                    showLoading(false)
                }
                is Result.Error -> {
                    showErrorMessage(result.error)
                    showLoading(false)
                }
            }
        }
        hideSearchView()
        binding.searchBar.setOnClickListener {
            showSearchView()
        }
    }

    private fun hideSearchView() {
        binding.searchView.visibility = View.INVISIBLE
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.editText.windowToken, 0)
    }

    private fun showSearchView() {
        binding.searchView.visibility = View.VISIBLE
        binding.searchView.editText.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView.editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun observeViewModel() {
        viewModel.getAllEvents(0).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    updateEventList(result.data)
                    showLoading(false)
                }
                is Result.Error -> {
                    showErrorMessage(result.error)
                    showLoading(false)
                }
            }
        }
    }

    private fun navigateToDetail(event: EventEntity) {
        val action = FinishedEventFragmentDirections.actionNavigationFinishedToDetailEventFragment(event.id)
        findNavController().navigate(action)
    }

    private fun updateEventList(events: List<EventEntity>) {
        if (events.isEmpty()) {
            showErrorMessage(getString(R.string.no_events_found_for_query, binding.searchView.text))
            binding.rvEvents.visibility = View.GONE
        } else {
            eventAdapter.submitList(events)
            binding.tvErrorMessage.visibility = View.GONE
            binding.rvEvents.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvEvents.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showErrorMessage(message: String) {
        binding.tvErrorMessage.text = message
        binding.tvErrorMessage.visibility = View.VISIBLE
        binding.rvEvents.visibility = View.GONE
        eventAdapter.submitList(emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}