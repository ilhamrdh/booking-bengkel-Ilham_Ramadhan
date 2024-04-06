package com.bengkel.booking.services;

import java.util.ArrayList;
import java.util.List;

import com.bengkel.booking.models.BookingOrder;
import com.bengkel.booking.models.Customer;
import com.bengkel.booking.models.ItemService;
import com.bengkel.booking.repositories.CustomerRepository;
import com.bengkel.booking.repositories.ItemServiceRepository;

public class MenuService {
	private static List<Customer> listAllCustomers = CustomerRepository.getAllCustomer();
	private static List<ItemService> listAllItemService = ItemServiceRepository.getAllItemService();
	private static List<BookingOrder> BookingOrderList = new ArrayList<BookingOrder>();
	private static String idCustomer = null;

	public static void run() {
		boolean isLooping = true;
		do {
			idCustomer = login();
			mainMenu();
		} while (isLooping);

	}

	public static String login() {
		String customer = BengkelService.Login(listAllCustomers);
		if (customer == null) {
			return login();
		}
		return customer;
	}

	public static void mainMenu() {
		String[] listMenu = { "Informasi Customer", "Booking Bengkel", "Top Up Bengkel Coin", "Informasi Booking",
				"Logout" };
		int menuChoice = 0;
		boolean isLooping = true;

		do {
			PrintService.printMenu(listMenu, "\nBooking Bengkel Menu");
			menuChoice = Validation.validasiNumberWithRange("Masukan Pilihan Menu:", "Input Harus Berupa Angka!",
					"^[0-9]+$", listMenu.length - 1, 0);

			switch (menuChoice) {
				case 1:
					// panggil fitur Informasi Customer
					System.out.println("\nProfile");
					System.out.println("===================================");
					BengkelService.CustomerInfo(listAllCustomers, idCustomer);
					break;
				case 2:
					// panggil fitur Booking Bengkel
					System.out.println("\nBooking Bengkel");
					System.out.println("===================================");
					BengkelService.Booking(listAllCustomers, listAllItemService, BookingOrderList, idCustomer);
					break;
				case 3:
					// panggil fitur Top Up Saldo Coin
					System.out.println("\nTop Up Saldo Coin");
					System.out.println("===================================");
					BengkelService.TopUpSaldoCoin(listAllCustomers, idCustomer);
					break;
				case 4:
					// panggil fitur Informasi Booking Order
					System.out.println("\nInformasi Booking Order");
					System.out.println("===================================");
					BengkelService.InformasiBookingOrder(listAllCustomers, BookingOrderList, idCustomer);
					break;
				default:
					System.out.println("Logout");
					BengkelService.Logout();
					break;
			}
		} while (isLooping);
	}

	// Silahkan tambahkan kodingan untuk keperluan Menu Aplikasi
}
