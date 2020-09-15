package com.mikeni.tictactoe.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.mikeni.tictactoe.R
import com.mikeni.tictactoe.databinding.FragmentGameBinding
import com.mikeni.tictactoe.model.Match
import com.mikeni.tictactoe.model.Player
import com.mikeni.tictactoe.util.SharedPreferencesHelper
import kotlinx.android.synthetic.main.fragment_game.*
import timber.log.Timber

class GameFragment : Fragment(), View.OnClickListener {

    private val args: GameFragmentArgs by navArgs()

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val preferencesHelper by lazy { SharedPreferencesHelper(requireContext()) }

    private lateinit var database: FirebaseDatabase
    private lateinit var matchReference: DatabaseReference
    private lateinit var match: Match

    private val buttons by lazy {
        arrayOf(
            binding.img00, binding.img01, binding.img02,
            binding.img10, binding.img11, binding.img12,
            binding.img20, binding.img21, binding.img22,
        )
    }

    companion object {
        private const val X = "X"
        private const val O = "O"
        private val combinations = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
        setListeners()
    }

    private fun setData() {
        setButtonsStatus(false)
        database = Firebase.database
        matchReference = database.getReference("server").child("matches").child(args.key)
        if (!args.isHost) {
            preferencesHelper.id?.let { key ->
                database.getReference("server").child("players").child(key)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.getValue<Player>()?.let {
                                matchReference.child("guest").setValue(it)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Timber.e(error.toException())
                        }
                    })
            }
        }
    }

    private fun setListeners() {
        matchReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.key == "guest") {
                    snapshot.getValue<Player>()?.let {
                        if (it.nickname.isNotBlank()) startGame()
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })

        matchReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<Match>()?.let {
                    match = it
                    match.board.move.mapIndexed { index, player ->
                        if (player.isNotBlank()) {
                            val drawable = if (player == X) R.drawable.ic_x else R.drawable.ic_o
                            when (index) {
                                0 -> binding.img00.setImageResource(drawable)
                                1 -> binding.img01.setImageResource(drawable)
                                2 -> binding.img02.setImageResource(drawable)
                                3 -> binding.img10.setImageResource(drawable)
                                4 -> binding.img11.setImageResource(drawable)
                                5 -> binding.img12.setImageResource(drawable)
                                6 -> binding.img20.setImageResource(drawable)
                                7 -> binding.img21.setImageResource(drawable)
                                8 -> binding.img22.setImageResource(drawable)
                            }
                        }
                    }
                    if (match.isPlaying) {
                        checkWinner()
                        updateStatus()
                    } else {
                        binding.tvPlayer.text = getString(R.string.label_match_waiting)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })

        buttons.forEach { it.setOnClickListener(this) }

        btnReset.setOnClickListener {
            findNavController().navigate(GameFragmentDirections.actionToRoomFragment())
        }
    }

    private fun startGame() {
        if (args.isHost) {
            matchReference.child("playing").setValue(true)
            matchReference.child("host").child("moving").setValue(true)
            matchReference.child("guest").child("moving").setValue(false)
        }
        binding.imgWaiting.cancelAnimation()
        binding.imgWaiting.visibility = View.GONE
        observePlayers()
    }

    private fun observePlayers() {
        matchReference.child("x").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<Boolean>()?.let { isX ->
                    setButtonsStatus(enable = isX == args.isHost)
                    if (isX == args.isHost) {
                        showSnackBar(getString(R.string.label_match_status))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })
    }

    override fun onClick(view: View?) {
        buttonSelected(view as ImageView)
    }

    private fun buttonSelected(imageView: ImageView) {
        when (imageView.id) {
            binding.img00.id -> updateBoard(0)
            binding.img01.id -> updateBoard(1)
            binding.img02.id -> updateBoard(2)
            binding.img10.id -> updateBoard(3)
            binding.img11.id -> updateBoard(4)
            binding.img12.id -> updateBoard(5)
            binding.img20.id -> updateBoard(6)
            binding.img21.id -> updateBoard(7)
            binding.img22.id -> updateBoard(8)
        }
    }

    private fun updateBoard(index: Int) {
        if (match.winner.isNotBlank()) {
            showSnackBar("Juego finalizado")
            return
        }
        match.board.move[index] = if (args.isHost) X else O
        match.isX = !match.isX
        matchReference.setValue(match)

        if (args.isHost) {
            matchReference.child("host").child("moving").setValue(false)
            matchReference.child("guest").child("moving").setValue(true)
        } else {
            matchReference.child("host").child("moving").setValue(true)
            matchReference.child("guest").child("moving").setValue(false)
        }
    }

    private fun checkWinner() {
        for (combination in combinations) {
            with(match.board) {
                val (a, b, c) = combination
                if (move[a].isNotBlank() && move[b].isNotBlank() && move[c].isNotBlank()) {
                    if (move[a] == move[b] && move[a] == move[c]) {
                        match.winner = move[a]
                        showSnackBar("GANADOR!!!")
                    }
                }
            }
        }
    }

    private fun updateStatus() {
        when {
            match.winner.isNotEmpty() -> {
                binding.tvPlayer.text = getString(R.string.label_match_winner, match.winner)
                binding.tvPlayer.setTextColor(Color.GREEN)
                binding.btnReset.visibility = View.VISIBLE
                setButtonsStatus(false)
            }
            match.board.move.filter { it.isNotBlank() }.size == 9 -> {
                binding.tvPlayer.text = getString(R.string.label_match_draw)
                binding.btnReset.visibility = View.VISIBLE
                setButtonsStatus(false)
            }
            else -> {
                val player = if (match.isX) X else O
                binding.tvPlayer.text = getString(R.string.label_match_player, player)
            }
        }
    }

    private fun setButtonsStatus(enable: Boolean) {
        buttons.forEach { it.isEnabled = enable }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (args.isHost) matchReference.removeValue()
        _binding = null
    }
}