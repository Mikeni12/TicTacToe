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
import com.google.firebase.ktx.Firebase
import com.mikeni.tictactoe.databinding.FragmentMenuBinding
import com.mikeni.tictactoe.model.Player
import com.mikeni.tictactoe.util.SharedPreferencesHelper
import timber.log.Timber

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val preferencesHelper by lazy { SharedPreferencesHelper(requireContext()) }

    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verifyNewPlayer()
    }

    private fun verifyNewPlayer() {
        if (preferencesHelper.nickname.isBlank()) {
            setData()
            setListeners()
        } else {
            findNavController().navigate(MenuFragmentDirections.actionToRoomFragment())
        }
    }

    private fun setData() {
        database = Firebase.database
        reference = database.getReference("server").child("players")
    }

    private fun setListeners() {
        binding.btnPlay.setOnClickListener {
            val nickname = binding.edtNickname.text.toString()
            when {
                nickname.isBlank() -> {
                    showSnackBar("El nombre de usuario no debe estar vacío")
                }
                nickname.contains(" ") -> {
                    showSnackBar("El nombre de usuario no debe contener espacios en blanco")
                }
                else -> {
                    verifyPlayer(nickname)
                }
            }
        }
    }

    private fun verifyPlayer(nickname: String) {
        reference.orderByChild("nickname").equalTo(nickname)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        showSnackBar("Ya existe este nombre de usuario")
                    } else {
                        createPlayer(nickname)
                        findNavController().navigate(MenuFragmentDirections.actionToRoomFragment())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })
    }

    private fun createPlayer(nickname: String) {
        val key = reference.push().key
        if (key == null) {
            showSnackBar("No se pudo crear el usuario")
            return
        }
        reference.child(key).setValue(Player(nickname))
        preferencesHelper.saveUserNickname(nickname)
        preferencesHelper.saveUserId(key)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}