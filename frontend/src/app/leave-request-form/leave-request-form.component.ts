import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NgFor, NgIf } from '@angular/common';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Employee, LeaveRequest } from '../models/leave-request.model';

export interface LeaveRequestFormValue {
  employeeId: number;
  startDate: string;
  endDate: string;
  type: number;
}

/** Group-level validator: startDate must not be after endDate. */
export const dateRangeValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const start = group.get('startDate')?.value;
  const end = group.get('endDate')?.value;
  // Date inputs yield ISO 'yyyy-MM-dd' strings, which compare correctly as text.
  return start && end && start > end ? { dateRange: true } : null;
};

@Component({
  selector: 'app-leave-request-form',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule],
  templateUrl: './leave-request-form.component.html',
  styleUrls: ['./leave-request-form.component.css']
})
export class LeaveRequestFormComponent implements OnInit {
  /** Emits the created request after the API accepted it. */
  @Output() submitted = new EventEmitter<LeaveRequest>();

  private leaveRequestsUrl = '/api/leave-requests';
  submitting = false;
  submitError = '';

  // Relative URL: the dev server proxies /api to the backend (see proxy.conf.json).
  private employeesUrl = '/api/employees';

  employees: Employee[] = [];
  employeesLoading = false;
  employeesError = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeesLoading = true;
    this.employeesError = '';
    this.http.get<Employee[]>(this.employeesUrl).subscribe({
      next: (data) => {
        this.employees = data;
        this.employeesLoading = false;
      },
      error: () => {
        this.employeesLoading = false;
        this.employeesError = 'Could not load employees.';
      }
    });
  }

  // Values match the numeric type codes used by the API (see typeLabel in the list).
  readonly vacationTypes = [
    { value: 0, label: 'Vacation' },
    { value: 1, label: 'Sick' },
    { value: 2, label: 'Unpaid' }
  ];

  readonly form = new FormGroup(
    {
      employeeId: new FormControl<number | null>(null, Validators.required),
      startDate: new FormControl('', { nonNullable: true, validators: Validators.required }),
      endDate: new FormControl('', { nonNullable: true, validators: Validators.required }),
      type: new FormControl<number | null>(null, Validators.required)
    },
    { validators: dateRangeValidator }
  );

  get employeeId() { return this.form.controls.employeeId; }
  get startDate() { return this.form.controls.startDate; }
  get endDate() { return this.form.controls.endDate; }
  get type() { return this.form.controls.type; }

  showError(control: AbstractControl): boolean {
    return control.invalid && (control.touched || control.dirty);
  }

  get showDateRangeError(): boolean {
    return this.form.hasError('dateRange') &&
      (this.startDate.touched || this.startDate.dirty) &&
      (this.endDate.touched || this.endDate.dirty);
  }

  onSubmit(): void {
    if (this.submitting) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { employeeId, startDate, endDate, type } = this.form.getRawValue();
    const body: LeaveRequestFormValue = {
      employeeId: employeeId as number,
      startDate,
      endDate,
      type: type as number
    };

    this.submitting = true;
    this.submitError = '';
    this.http.post<LeaveRequest>(this.leaveRequestsUrl, body).subscribe({
      next: (created) => {
        this.submitting = false;
        this.form.reset();
        this.submitted.emit(created);
      },
      error: (err: HttpErrorResponse) => {
        // Keep the entered values so the user can correct and resubmit.
        this.submitting = false;
        this.submitError = this.errorText(err);
      }
    });
  }

  /** Extracts the error text returned by the API, whatever shape it comes in. */
  private errorText(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Could not reach the server. Please try again.';
    }
    let body = err.error;
    if (typeof body === 'string') {
      try {
        body = JSON.parse(body);
      } catch {
        // Plain-text error body.
      }
    }
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (body && typeof body === 'object') {
      // Field-level validation errors, e.g. { errors: { endDate: ['...'] } } or [{ defaultMessage }]
      const errors = body.errors;
      if (errors && typeof errors === 'object') {
        const messages = (Array.isArray(errors) ? errors : Object.values(errors))
          .flat()
          .map((e: any) => (typeof e === 'string' ? e : e?.defaultMessage ?? e?.message))
          .filter((m: unknown) => typeof m === 'string' && m);
        if (messages.length) {
          return messages.join(' ');
        }
      }
      for (const key of ['message', 'detail', 'error', 'title']) {
        if (typeof body[key] === 'string' && body[key]) {
          return body[key];
        }
      }
    }
    return 'Failed to submit the request (HTTP ' + err.status + ').';
  }
}
