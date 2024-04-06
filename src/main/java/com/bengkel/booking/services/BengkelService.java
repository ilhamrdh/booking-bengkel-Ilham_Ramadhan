package com.bengkel.booking.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.bengkel.booking.models.BookingOrder;
import com.bengkel.booking.models.Customer;
import com.bengkel.booking.models.ItemService;
import com.bengkel.booking.models.MemberCustomer;
import com.bengkel.booking.models.Vehicle;

public class BengkelService {

	private static final Scanner input = new Scanner(System.in);

	// Silahkan tambahkan fitur-fitur utama aplikasi disini

	// Login
	public static String Login(List<Customer> customerList) {
		System.out.println("Login");
		System.out.println("==================");
		System.out.print("Masukan id customer: ");
		String customerId = input.nextLine();
		Customer customer = customerList.stream()
				.filter(cust -> cust instanceof Customer)
				.map(cust -> (Customer) cust)
				.filter(cust -> cust.getCustomerId().equals(customerId))
				.findFirst()
				.orElse(null);

		if (customer == null) {
			System.err.println("\nId customer tidak ditemukan\n");
			return null;
		}

		System.out.print("Masukan password: ");
		String password = input.nextLine();
		Customer pass = customerList.stream()
				.filter(cust -> cust.getPassword().equals(password))
				.findFirst()
				.orElse(null);

		if (pass == null) {
			System.err.println("\nPassword salah\n");
			return null;
		}
		return customer.getCustomerId();
	}

	// Info Customer
	public static void CustomerInfo(List<Customer> customerList, String id) {
		Customer customer = customerList.stream()
				.filter(cust -> cust instanceof Customer)
				.map(cust -> (Customer) cust)
				.filter(cust -> cust.getCustomerId().equals(id))
				.findFirst()
				.orElse(null);

		if (customer == null) {
			System.err.println("\ncustomer tidak ditemukan\n");
		}
		MemberCustomer memberCustomer = customerList.stream()
				.filter(cust -> cust instanceof MemberCustomer)
				.map(cust -> (MemberCustomer) cust)
				.filter(member -> member.getCustomerId().equals(customer.getCustomerId()))
				.findFirst().orElse(null);

		System.out.println("Customer Id: " + customer.getCustomerId());
		System.out.println("Nama: " + customer.getName());
		System.out.println("Alamat: " + customer.getAddress());

		if (memberCustomer != null) {
			System.out.println("Customer Status: Member");
			System.out.println("Saldo Koin: " + memberCustomer.getSaldoCoin());
		} else {
			System.out.println("Customer Status: Non Member");
		}

		System.out.println("List Kendaraan:");
		PrintService.printVechicle(customer.getVehicles());
	}

	// Booking atau Reservation
	public static void Booking(List<Customer> customerList, List<ItemService> listService,
			List<BookingOrder> bookingOrders, String id) {
		Customer customer = findCustomer(customerList, id);
		if (customer == null) {
			System.err.println("Customer tidak di ada");
			return;
		}

		List<Vehicle> vehicles = customer.getVehicles();
		System.out.println("Kendaraan yang dimiliki:");
		vehicles.stream()
				.forEach(vehicle -> {
					System.out.println("tipe kendaraan: " + vehicle.getVehicleType() + ", Nomor Kendaraan: "
							+ vehicle.getVehiclesId());
				});

		System.out.print("Masukan Vehicle Id: ");
		String vechileId = input.nextLine();

		Vehicle selectedVehicle = vehicles.stream()
				.filter(vehicle -> vehicle.getVehiclesId().equals(vechileId))
				.findFirst()
				.orElse(null);

		if (selectedVehicle == null) {
			System.err.println("Bukan kendaraan anda");
			return;
		}
		System.out.println("\nService yang tersedia untuk " + selectedVehicle.getVehicleType());

		listService.stream()
				.filter(item -> item.getVehicleType().equals(selectedVehicle.getVehicleType()))
				.forEach(service -> {
					System.out.println("Service Id: " + service.getServiceId() + ", Service Name: "
							+ service.getServiceName() + ", Price: " + service.getPrice());
				});

		List<ItemService> selectedService = new ArrayList<>();
		boolean addService = true;
		while (addService) {
			System.out.print("Masukan id Services: ");
			String serviceId = input.nextLine();

			ItemService service = listService.stream()
					.filter(item -> item.getServiceId().equals(serviceId))
					.findFirst()
					.orElse(null);

			if (service == null) {
				System.err.println("Service tidak ditemukan");
				continue;
			}

			selectedService.add(service);

			System.out.println("Apakah anda ingin menambahkan Service Lainnya? (Y/T) : ");
			String confirm = input.nextLine();

			if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("Y")) {
				addService = true;
			} else if (confirm.equalsIgnoreCase("t") || confirm.equalsIgnoreCase("T")) {
				addService = false;
			} else {
				System.err.println("Masukan Y atau T");
				continue;
			}
		}

		double totalPayment = selectedService.stream().mapToDouble(ItemService::getPrice).sum();
		System.out.println("Total biaya service: " + totalPayment);

		System.out.println("Silahkan Pilih Metode Pembayaran (Saldo Coin atau Cash)");
		System.out.println("1. Saldo Coin");
		System.out.println("2. Cash");
		int choice = Validation.validasiNumberWithRange("Pilih Metode pembawaran: ", "Invalid Input", "^[1-2]$", 2, 1);

		String paymentMethod = "";
		switch (choice) {
			case 1:
				if (customer instanceof MemberCustomer) {
					MemberCustomer member = (MemberCustomer) customer;
					if (member.getSaldoCoin() >= totalPayment) {
						paymentMethod = "Saldo Coin";
					} else {
						System.out.println("Saldo koin tidak cukup");
						return;
					}
				} else {
					System.out.println("Saldo Coin hanya dapat digunakan oleh membership");
					return;
				}
				break;
			case 2:
				paymentMethod = "Cash";
				break;
			default:
				break;
		}
		BookingOrder booking = BookingOrder.builder()
				.bookingId("Book-Cust-00" + (bookingOrders.size() + 1) + "-" + id)
				.customer(customer)
				.services(selectedService)
				.paymentMethod(paymentMethod)
				.totalServicePrice(totalPayment)
				.totalPayment(0)
				.build();

		booking.calculatePayment();
		bookingOrders.add(booking);
		System.out.println("Booking Berhasil!!!");
		System.out.println("Total Harga Service: " + totalPayment);
		System.out.println("Total Pembayaran: " + booking.getTotalPayment());
	}

	// Top Up Saldo Coin Untuk Member Customer
	public static void TopUpSaldoCoin(List<Customer> customerList, String id) {
		Customer customer = findCustomer(customerList, id);
		if (customer == null) {
			System.err.println("Customer tidak di ada");
			return;
		}
		if (!(customer instanceof MemberCustomer)) {
			System.err.println("Maaf fitur ini hanya untuk Member saja!");
			return;
		}

		MemberCustomer member = (MemberCustomer) customer;
		String input = Validation.validasiInput("Masukan Jumlah Top Up Saldo Coin:  ", "Invalid input",
				"^[0-9]+(\\.[0-9]+)?$");
		double amount = Double.parseDouble(input);

		if (amount <= 0) {
			System.err.println("Invalid amount");
		}

		member.setSaldoCoin(member.getSaldoCoin() + amount);
		System.out.println("Top-up Saldo Coin Berhasil");
		System.out.println("Saldo saat ini:" + member.getSaldoCoin());
	}

	// Informasi Booking Order
	public static void InformasiBookingOrder(List<Customer> customerList, List<BookingOrder> bookingOrders, String id) {
		Customer customer = findCustomer(customerList, id);
		if (customer == null) {
			System.err.println("Customer tidak di ada");
			return;
		}
		bookingOrders.stream()
				.filter(booking -> booking.getCustomer().getCustomerId().equals(customer.getCustomerId()))
				.forEach(booking -> {
					System.out.println("\n================================================================\n");
					System.out.println("No : " + (bookingOrders.indexOf(booking) + 1));
					System.out.println("Nama Customer : " + booking.getCustomer().getName());
					System.out.println("Payment Methods : " + booking.getPaymentMethod());
					System.out.println("Total Service: " + booking.getTotalServicePrice());
					System.out.println("Total Payment: " + booking.getTotalPayment());
					System.out.print("List Services : ");
					booking.getServices().forEach(service -> {
						System.out.print(service.getServiceName() + ",");
					});
				});
	}

	// Logout
	public static void Logout() {
		System.exit(0);
	}

	private static Customer findCustomer(List<Customer> customerList, String id) {
		return customerList.stream()
				.filter(cust -> cust.getCustomerId().equals(id))
				.findFirst()
				.orElse(null);
	}

}
