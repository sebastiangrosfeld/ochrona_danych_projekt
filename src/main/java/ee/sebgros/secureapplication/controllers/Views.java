package ee.sebgros.secureapplication.controllers;

import ee.sebgros.secureapplication.data.AddPlayerAccess;
import ee.sebgros.secureapplication.data.CreateNoteRequest;
import ee.sebgros.secureapplication.data.DecryptRequest;
import ee.sebgros.secureapplication.repository.entity.Note;
import ee.sebgros.secureapplication.repository.entity.User;
import ee.sebgros.secureapplication.repository.entity.UserWithAccess;
import ee.sebgros.secureapplication.repository.repositories.AccessGrantedRepository;
import ee.sebgros.secureapplication.repository.repositories.NoteRepository;
import ee.sebgros.secureapplication.services.AESService;
import ee.sebgros.secureapplication.services.HtmlSanitizer;
import ee.sebgros.secureapplication.services.NoteAccessVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Locale;
import java.util.Objects;

@Controller
@Slf4j
@RequiredArgsConstructor
public class Views {
	private final NoteRepository noteRepository;
	private final AccessGrantedRepository accessGrantedRepository;
	private final AESService aesService;
	private final HtmlSanitizer htmlSanitizer;
	private final NoteAccessVerifier noteAccessVerifier;

	@GetMapping("/")
	public String getMain(@AuthenticationPrincipal User user, Model model) {
		noteAccessVerifier.addNotes(user, model);
		return "main";
	}

	@GetMapping("/create_note")
	public String getCreateNote(Model model) {

		model.addAttribute("createNoteRequest", new CreateNoteRequest());
		return "create_note";
	}

	@GetMapping("/show_note/{note_id}")
	public String getShowNote(@AuthenticationPrincipal User user, Model model, @PathVariable("note_id") Long noteId) {
		noteAccessVerifier.addNotes(user, model);

		Note note = noteRepository.findById(noteId).orElse(null);
		if (note == null) {
			model.addAttribute("element_not_found", true);
			return "main";
		}
		if (!noteAccessVerifier.verifyHaveAccess(user, note, model)) {
			model.addAttribute("element_not_found", true);
			return "main";
		}
		if (note.getIsEncrypted()) {
			return "redirect:/decode_note/" + noteId;
		}
		model.addAttribute("note", note);
		return "show_note";
	}

	@PostMapping("/show_note/{note_id}")
	public String postShowNote(@AuthenticationPrincipal User user, @PathVariable("note_id") Long noteId, Model model, @Valid DecryptRequest decryptRequest, Errors errors) {
		noteAccessVerifier.addNotes(user, model);
		model.addAttribute("id", noteId);

		Note note = noteRepository.findById(noteId).orElse(null);
		if (note == null) {
			model.addAttribute("element_not_found", true);
			return "main";
		}
		if (!noteAccessVerifier.verifyHaveAccess(user, note, model)) {
			model.addAttribute("element_not_found", true);
			return "main";
		}


		if (!note.getIsEncrypted()) {
			return "redirect:/show_note/" + noteId;
		}
		if (errors.hasErrors()) {
			model.addAttribute("error", true);

			return "decrypt_note";
		}
		try {
			Thread.sleep(1000);
			note.setNote(aesService.decrypt(decryptRequest.getPassword(), note.getNote()));
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", true);

			return "decrypt_note";
		}
		String note_text = htmlSanitizer.sanitize(note.getNote());
		note.setNote(note_text);
		model.addAttribute("note", note);
		return "show_note";
	}

	@PostMapping("/add_player/{note_id}")
	public String postAddPlayer(@AuthenticationPrincipal User user, @PathVariable("note_id") Long noteId,
	                            @Valid AddPlayerAccess addPlayerAccess, Errors errors, Model model) {
		noteAccessVerifier.addNotes(user, model);

		Note note = noteRepository.findById(noteId).orElse(null);
		if (note == null || errors.hasErrors()) {
			model.addAttribute("element_not_found", true);
			return "main";
		}

		if (!noteAccessVerifier.verifyHaveAccess(user, note, model)) {
			model.addAttribute("element_not_found", true);
			return "main";
		}

		if (!Objects.equals(note.getUser().getUsername(), user.getUsername())) {
			model.addAttribute("element_not_found", true);
			return "main";
		}

		if (note.getUsersWithAccess().stream().anyMatch(o -> o.getName().toUpperCase(Locale.ROOT).equals(addPlayerAccess.getName().toUpperCase(Locale.ROOT)))) {
			return "redirect:/show_note/" + noteId;
		}

		UserWithAccess userToAdd = new UserWithAccess();
		userToAdd.setName(addPlayerAccess.getName());
		userToAdd = accessGrantedRepository.save(userToAdd);

		note.getUsersWithAccess().add(userToAdd);
		noteRepository.save(note);
		log.info("USER ADDED");
		return "redirect:/show_note/" + noteId;
	}

	@GetMapping("/decode_note/{note_id}")
	public String getDecryptNote(@AuthenticationPrincipal User user, Model model, @PathVariable("note_id") Long noteId) {
		noteAccessVerifier.addNotes(user, model);

		Note note = noteRepository.findById(noteId).orElse(null);
		if (note == null) {
			model.addAttribute("element_not_found", true);
			return "main";
		}

		if (!noteAccessVerifier.verifyHaveAccess(user, note, model)) {
			model.addAttribute("element_not_found", true);
			return "main";
		}

		model.addAttribute("decryptRequest", new DecryptRequest());
		model.addAttribute("id", noteId);
		return "decrypt_note";
	}

	@PostMapping("/create_note")
	public String postCreateNote(@AuthenticationPrincipal User user, @Valid CreateNoteRequest createNoteRequest, Errors errors
			, Model model) {
		noteAccessVerifier.addNotes(user, model);

		if (errors.hasErrors()) {
			return "create_note";
		}
		Note note = new Note();
		note.setName(createNoteRequest.getName());
		String note_text = htmlSanitizer.sanitize(createNoteRequest.getNote());
		if (createNoteRequest.getPassword().length() > 0) {
			try {
				String result = aesService.encrypt(createNoteRequest.getPassword(), note_text);
				note.setNote(result);
			} catch (Exception e) {
				log.error(e.toString());
				model.addAttribute("createNoteError", true);
				return "create_note";
			}
			note.setIsEncrypted(true);
		} else {
			note.setNote(note_text);
			note.setIsEncrypted(false);
		}
		note.setUser(user);
		note.setIsPublic(createNoteRequest.getIsPublic());
		noteRepository.save(note);
		return "redirect:/";
	}
}
