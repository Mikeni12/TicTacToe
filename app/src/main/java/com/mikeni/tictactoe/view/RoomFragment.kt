package com.mikeni.tictactoe.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.mikeni.tictactoe.MatchesAdapter
import com.mikeni.tictactoe.databinding.FragmentRoomBinding
import com.mikeni.tictactoe.model.Match
import com.mikeni.tictactoe.model.Player
import com.mikeni.tictactoe.util.SharedPreferencesHelper
import timber.log.Timber

class RoomFragment : Fragment() {

    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!

    private val preferencesHelper by lazy { SharedPreferencesHelper(requireContext()) }
    private val adapter by lazy { MatchesAdapter() }

    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
        setListeners()
    }

    private fun setData() {
        database = Firebase.database
        reference = database.getReference("server").child("matches")
        binding.recyclerMatches.adapter = adapter
    }

    private fun setListeners() {
        binding.btnAddRoom.setOnClickListener {
            createMatch()
        }

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adapter.submitList(
                    snapshot.children.mapNotNull { it.getValue<Match>() }.filter { !it.isPlaying }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })
    }

    private fun createMatch() {
        val key = reference.push().key
        if (key == null) {
            showSnackBar("No se pudo crear la partida")
            return
        }
        reference.child(key).setValue(Match(key, Player(preferencesHelper.nickname)))
        findNavController().navigate(RoomFragmentDirections.actionToGameFragment(key))
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}