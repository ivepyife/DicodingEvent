package com.dicoding.dicodingevent.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.dicoding.dicodingevent.R
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.databinding.FragmentDetailEventBinding
import com.dicoding.dicodingevent.ui.MainViewModel
import com.dicoding.dicodingevent.ui.ViewModelFactory
import com.dicoding.dicodingevent.data.source.Result
import kotlinx.coroutines.launch

class DetailEventFragment : Fragment() {
    private var _binding: FragmentDetailEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private val args: DetailEventFragmentArgs by navArgs()
    private var currentEvent: EventEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFavoriteButton()
        loadEventDetails()
    }

    private fun setupFavoriteButton() {
        binding.fabFavorite.setOnClickListener {
            currentEvent?.let { event ->
                viewModel.viewModelScope.launch {
                    if (event.isFavorite) {
                        viewModel.deleteEvent(event)
                        updateFavoriteButtonState(false)
                        showToast("Dihapus dari favorit")
                    } else {
                        viewModel.setFavoriteEvent(event, true)
                        updateFavoriteButtonState(true)
                        showToast("Ditambahkan ke favorit")
                    }
                    currentEvent = event.copy(isFavorite = !event.isFavorite)
                }
            }
        }
    }

    private fun loadEventDetails() {
        showLoading(true)

        viewModel.getEventById(args.eventId).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    currentEvent = result.data
                    updateUI(result.data)
                    updateFavoriteButtonState(result.data.isFavorite)
                    showLoading(false)
                }
                is Result.Error -> {
                    showToast(result.error)
                    showLoading(false)
                }
                is Result.Loading -> showLoading(true)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(event: EventEntity) {
        binding.apply {
            tvName.text = event.name
            tvOwner.text = "Penyelenggara: ${event.ownerName}"
            tvBeginTime.text = "Waktu Mulai: ${event.beginTime}"
            tvQuotaRemaining.text = "Kuota: ${event.quota - event.registrants}"
            tvDescription.text = if (event.description.isNotEmpty()) {
                HtmlCompat.fromHtml(event.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                "Deskripsi acara tidak tersedia"
            }

            Glide.with(requireContext())
                .load(event.imageLogo)
                .placeholder(R.drawable.baseline_image_24)
                .error(R.drawable.baseline_broken_image_24)
                .into(imgEvent)

            btnLink.setOnClickListener {
                if (event.link.isNotEmpty()) {
                    Intent(Intent.ACTION_VIEW, Uri.parse(event.link)).also { intent ->
                        startActivity(intent)
                    }
                } else {
                    showToast("Link acara tidak tersedia")
                }
            }
        }
    }

    private fun updateFavoriteButtonState(isFavorite: Boolean) {
        binding.fabFavorite.setImageResource(
            if (isFavorite) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}